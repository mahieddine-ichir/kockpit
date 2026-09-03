#!/bin/bash
# =============================================================================
# gh_artifacts_and_images.sh
# Interactive helper to list/download GitHub Actions artifacts and GHCR
# container images for the current repo, via the gh CLI.
#
# Usage:
#   ./gh_artifacts_and_images.sh
#
# Requires: gh CLI, authenticated (gh auth login). Listing container packages
# additionally requires the read:packages token scope - the script offers to
# add it via `gh auth refresh` if missing.
# =============================================================================

set -euo pipefail

# --- Colors ------------------------------------------------------------------
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# --- Preconditions -------------------------------------------------------------
require_gh() {
  if ! command -v gh >/dev/null 2>&1; then
    echo -e "${RED}gh CLI not found. Install: https://cli.github.com${NC}"
    exit 1
  fi
  if ! gh auth status >/dev/null 2>&1; then
    echo -e "${RED}Not authenticated. Run: gh auth login${NC}"
    exit 1
  fi
}

# --- Workflow artifacts --------------------------------------------------------
list_all_artifacts() {
  echo -e "${CYAN}Fetching all workflow artifacts (paginated)...${NC}"
  gh api repos/{owner}/{repo}/actions/artifacts --paginate \
    --jq '.artifacts[] | "\(.id)\t\(.name)\t\(.size_in_bytes) bytes\t\(.created_at)\t\(if .expired then "EXPIRED" else "active" end)"' \
    | column -t -s $'\t'
}

list_run_artifacts() {
  read -rp "Workflow run ID: " run_id
  if [ -z "$run_id" ]; then
    echo -e "${YELLOW}No run ID given.${NC}"
    return
  fi
  gh api "repos/{owner}/{repo}/actions/runs/${run_id}/artifacts" \
    --jq '.artifacts[] | "\(.id)\t\(.name)\t\(.size_in_bytes) bytes\t\(if .expired then "EXPIRED" else "active" end)"' \
    | column -t -s $'\t'
}

download_run_artifacts() {
  read -rp "Workflow run ID to download artifacts from: " run_id
  if [ -z "$run_id" ]; then
    echo -e "${YELLOW}No run ID given.${NC}"
    return
  fi
  read -rp "Destination dir [./artifacts-${run_id}]: " dest
  dest=${dest:-./artifacts-${run_id}}
  gh run download "$run_id" --dir "$dest"
  echo -e "${GREEN}✅ Downloaded to ${dest}${NC}"
}

# --- Container (Docker) packages ------------------------------------------------
ensure_packages_scope() {
  if gh api /user/packages?package_type=container >/dev/null 2>&1; then
    return 0
  fi
  echo -e "${YELLOW}Your gh token is missing the 'read:packages' scope.${NC}"
  read -rp "Run 'gh auth refresh -s read:packages' now? [y/N] " ans
  if [[ "$ans" =~ ^[Yy]$ ]]; then
    gh auth refresh -s read:packages
  else
    echo -e "${YELLOW}Skipped - can't list packages without that scope.${NC}"
    return 1
  fi
}

list_container_packages() {
  ensure_packages_scope || return
  echo -e "${CYAN}Fetching container packages for the authenticated user...${NC}"
  gh api "/user/packages?package_type=container" \
    --jq '.[] | "\(.name)\t\(.visibility)\t\(.updated_at)"' \
    | column -t -s $'\t'
}

list_package_versions() {
  ensure_packages_scope || return
  read -rp "Package name (e.g. kockpit/kockpit-audit-stream-application-aws): " pkg
  if [ -z "$pkg" ]; then
    echo -e "${YELLOW}No package name given.${NC}"
    return
  fi
  local encoded
  encoded=$(printf '%s' "$pkg" | sed 's#/#%2F#g')
  gh api "/user/packages/container/${encoded}/versions" \
    --jq '.[] | "\(.id)\t\(.metadata.container.tags // [])\t\(.created_at)"' \
    | column -t -s $'\t'
}

# --- Menu ------------------------------------------------------------------
menu() {
  echo ""
  echo -e "${BOLD}GitHub Artifacts & Container Images${NC}"
  echo "  1) List all workflow artifacts"
  echo "  2) List artifacts for a specific run"
  echo "  3) Download artifacts from a run"
  echo "  4) List container (Docker) packages"
  echo "  5) List tags/versions for a container package"
  echo "  q) Quit"
  echo ""
  read -rp "Choice: " choice
  case "$choice" in
    1) list_all_artifacts ;;
    2) list_run_artifacts ;;
    3) download_run_artifacts ;;
    4) list_container_packages ;;
    5) list_package_versions ;;
    q|Q) exit 0 ;;
    *) echo -e "${YELLOW}Invalid choice.${NC}" ;;
  esac
}

require_gh
while true; do
  menu
done
