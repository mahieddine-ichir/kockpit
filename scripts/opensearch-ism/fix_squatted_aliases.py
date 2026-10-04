#!/usr/bin/env python3
"""
Turn concrete indices that squat a write-alias name back into a real alias, keeping their data.

How the squatting happens: if a bulk write targets "<prefix>-write" at a moment when that alias
doesn't exist, OpenSearch auto-creates a concrete index with that name. From then on the app can
never attach the alias (invalid_alias_name_exception), its daily indices stay empty, and the
squatting index silently receives every write for the prefix.

For each concrete index X named "<prefix>-write" this script:
  1. blocks writes on X (bulk writes to it fail for the duration - the reports stay in S3),
  2. clones X to "<prefix>-<X's creation day>-recovered" (hard-links segments: fast, no copy),
     and checks the clone has the same document count,
  3. in ONE atomic _aliases request: deletes X, points the write alias "<prefix>-write" at today's
     daily index "<prefix>-<today UTC>-00001", and adds the clone to the read alias - so there is
     no instant where the alias name is free for another auto-creation,
  4. puts the clone on the delete-only policy for its TTL (audit_delete_only_ttl<N>d). The clone's
     creation date is now, so its data is kept ~(now - X's creation) longer than the TTL.

If anything fails before step 3, writes on X are unblocked again and X is left as it was.

  ./fix_squatted_aliases.py --host https://localhost:9200 --insecure              # plan
  ./fix_squatted_aliases.py --host https://localhost:9200 --insecure --apply      # asks to confirm

Prevent new ones first (cluster setting, also protects older app versions):
  PUT _cluster/settings {"persistent": {"action.auto_create_index": "-*-write,-.aws_cold_catalog*,+*"}}
"""
import argparse
import json
import re
import ssl
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from datetime import datetime, timezone

POLICY_PREFIX = "audit_delete_only_ttl"


class OpenSearch:
    def __init__(self, host, insecure):
        self.host = host.rstrip("/")
        self.ctx = ssl._create_unverified_context() if insecure else None

    def request(self, method, path, body=None, ok_404=False, timeout=300):
        req = urllib.request.Request(self.host + path, method=method,
                                     data=json.dumps(body).encode() if body is not None else None,
                                     headers={"Content-Type": "application/json"})
        try:
            with urllib.request.urlopen(req, timeout=timeout, context=self.ctx) as r:
                return json.loads(r.read() or b"{}")
        except urllib.error.HTTPError as e:
            if ok_404 and e.code == 404:
                return None
            raise RuntimeError(f"{method} {path} -> HTTP {e.code}: {e.read().decode(errors='replace')[:1000]}")
        except urllib.error.URLError as e:
            sys.exit(f"❌ {method} {path} -> {e.reason} (is the tunnel open?)")


def delete_only_policy(ttl):
    return {"policy": {
        "description": f"Delete {ttl} after index creation, no rollover. For indices whose rollover "
                       f"can't run because the audit stream manages their write alias.",
        "default_state": "hot",
        "states": [
            {"name": "hot", "actions": [],
             "transitions": [{"state_name": "delete", "conditions": {"min_index_age": ttl}}]},
            {"name": "delete", "actions": [{"delete": {}}], "transitions": []},
        ],
    }}


def plan_for(os_, row, today):
    squatter = row["index"]
    prefix = squatter[:-len("-write")]
    ttl_match = re.search(r"-ttl(\d+d)$", prefix)
    created = datetime.fromtimestamp(int(row["creation.date"]) / 1000, timezone.utc)
    return {
        "squatter": squatter,
        "alias": squatter,
        "docs": int(row["docs.count"] or 0),
        "size": int(row["pri.store.size"] or 0),
        "created": created,
        "clone": f"{prefix}-{created:%Y.%m.%d}-recovered",
        "today": f"{prefix}-{today}-00001",
        "read_alias": re.sub(r"-ttl\d+d$", "", prefix) + "-read",
        "ttl": ttl_match.group(1) if ttl_match else None,
    }


def count(os_, index):
    os_.request("POST", f"/{index}/_refresh")
    return os_.request("GET", f"/{index}/_count")["count"]


def repair(os_, p):
    x, clone = p["squatter"], p["clone"]
    print(f"\n➡️  {x}")
    if os_.request("GET", f"/{p['today']}", ok_404=True) is None:
        os_.request("PUT", f"/{p['today']}", {})
        print(f"   created today's index {p['today']}")

    os_.request("PUT", f"/{x}/_settings", {"index.blocks.write": True})
    print("   writes blocked")
    try:
        # Keep the source's replica count: a clone otherwise gets the default (1), and the cluster
        # then copies a full replica of what can be a ~100 GB index onto already-full nodes.
        replicas = next(iter(os_.request("GET", f"/{x}/_settings/index.number_of_replicas").values()))
        replicas = replicas["settings"]["index"]["number_of_replicas"]
        os_.request("POST", f"/{x}/_clone/{clone}?wait_for_active_shards=1",
                    {"settings": {"index.blocks.write": False, "index.number_of_replicas": replicas}}, timeout=900)
        os_.request("GET", f"/_cluster/health/{clone}?wait_for_status=yellow&timeout=600s", timeout=900)
        source_docs, clone_docs = count(os_, x), count(os_, clone)
        if source_docs != clone_docs:
            raise RuntimeError(f"clone has {clone_docs} docs, source {source_docs}")
        print(f"   cloned to {clone} ({clone_docs} docs, same as source)")
    except Exception:
        os_.request("PUT", f"/{x}/_settings", {"index.blocks.write": False})
        # The clone (if it got created) is ours from this run - main() refuses to start when the
        # name already exists - so it's safe to drop; leaving it would block a re-run.
        os_.request("DELETE", f"/{clone}", ok_404=True)
        print(f"   ❌ clone failed - writes on {x} unblocked, partial clone removed, {x} unchanged")
        raise

    # One atomic request: the squatter disappears and the alias appears in the same cluster-state
    # update, so no bulk write can auto-create the name again in between.
    os_.request("POST", "/_aliases", {"actions": [
        {"remove_index": {"index": x}},
        {"add": {"index": p["today"], "alias": p["alias"], "is_write_index": True}},
        {"add": {"index": p["today"], "alias": p["read_alias"]}},
        {"add": {"index": clone, "alias": p["read_alias"]}},
    ]})
    print(f"   ✅ {p['alias']} is now an alias -> {p['today']} (write); {clone} added to {p['read_alias']}")

    if p["ttl"]:
        policy_id = f"{POLICY_PREFIX}{p['ttl']}"
        if os_.request("GET", f"/_plugins/_ism/policies/{policy_id}", ok_404=True) is None:
            os_.request("PUT", f"/_plugins/_ism/policies/{policy_id}", delete_only_policy(p["ttl"]))
        os_.request("POST", f"/_plugins/_ism/remove/{clone}")
        added = os_.request("POST", f"/_plugins/_ism/add/{clone}", {"policy_id": policy_id})
        state = "❌ " + json.dumps(added.get("failed_indices")) if added.get("failures") else "✅"
        print(f"   {state} {clone} on ISM policy {policy_id}")


def main():
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("--host", default="https://localhost:9200")
    p.add_argument("--insecure", action="store_true", help="Skip TLS verification (tunnel)")
    p.add_argument("--index", action="append", help="Only these squatting indices (repeatable)")
    p.add_argument("--apply", action="store_true")
    p.add_argument("--yes", action="store_true")
    args = p.parse_args()
    os_ = OpenSearch(args.host, args.insecure)

    rows = os_.request("GET", "/_cat/indices?format=json&bytes=b&h=index,docs.count,pri.store.size,creation.date")
    aliases = set()
    for a in os_.request("GET", "/_alias").values():
        aliases |= set(a.get("aliases", {}))
    squatters = [r for r in rows if r["index"].endswith("-write") and not r["index"].startswith(".")
                 and (not args.index or r["index"] in args.index)]
    if not squatters:
        print("No concrete index named like a write alias.")
        return

    today = datetime.now(timezone.utc).strftime("%Y.%m.%d")
    plans = [plan_for(os_, r, today) for r in sorted(squatters, key=lambda r: r["index"])]
    print(f"{'squatting index':<45} {'docs':>12} {'size':>9}  created           -> clone / write alias target")
    for pl in plans:
        print(f"{pl['squatter']:<45} {pl['docs']:>12} {pl['size'] / 1e9:>7.1f}GB  {pl['created']:%Y-%m-%d %H:%M}  "
              f"-> {pl['clone']} / {pl['today']}")
        if pl["clone"] in {r["index"] for r in rows} or pl["clone"] in aliases:
            sys.exit(f"❌ {pl['clone']} already exists - nothing changed")

    if not args.apply:
        print("\nDry run - nothing changed. Re-run with --apply.")
        return
    if not args.yes:
        try:
            answer = input(f"\nRepair {len(plans)} index(es) on {args.host}? Writes to each are blocked "
                           f"while it is cloned. Type APPLY to confirm: ")
        except EOFError:
            sys.exit("No terminal to confirm from - nothing changed (use --yes).")
        if answer.strip() != "APPLY":
            sys.exit("Aborted - nothing changed.")

    failed = 0
    for pl in plans:
        try:
            repair(os_, pl)
        except Exception as e:
            failed += 1
            print(f"   ❌ {pl['squatter']}: {e}")
    print(f"\n{'✅' if not failed else '⚠️ '} {len(plans) - failed}/{len(plans)} repaired.")
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
