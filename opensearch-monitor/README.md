# opensearch-monitor

A minimal OpenSearch monitoring app: an Express backend proxies read-only OpenSearch
`_cat` APIs, a React/Vite frontend renders the results. Both are packaged into a
single Docker image.

First use case: `GET _cat/allocation?v&s=disk.percent:desc` rendered as a pie chart
(disk % per node, top 7 individually + an "Other" slice), plus the full raw table.

## Project layout

```
opensearch-monitor/
├── backend/    # Express API (proxies OpenSearch, serves the built frontend)
├── frontend/   # React/Vite app (Chart.js pie chart)
└── Dockerfile  # multi-stage build → one image, one container
```

## Local development (no Docker)

Requires an OpenSearch cluster reachable at `OPENSEARCH_URL` (defaults to
`http://localhost:9200`). This repo's own local stack at
`deployment/local-dev-docker/docker/full` runs one with security disabled:

```bash
cd deployment/local-dev-docker/docker/full
docker compose up -d opensearch
```

Then, in two terminals:

```bash
# backend (API on :3000)
cd opensearch-monitor/backend
npm install
npm run dev

# frontend (dev server on :5173, proxies /api to :3000)
cd opensearch-monitor/frontend
npm install
npm run dev
```

Open http://localhost:5173.

## Run with Docker (single container, front + back together)

```bash
cd opensearch-monitor
docker compose up --build
```

By default this points at `http://host.docker.internal:9200` (a cluster running on
your host machine, e.g. the local dev stack above). Override with a `.env` file
(see `.env.example`) or `OPENSEARCH_URL=... docker compose up --build`.

Open http://localhost:3000.

Or build/run the image directly:

```bash
docker build -t opensearch-monitor .
docker run -p 3000:3000 -e OPENSEARCH_URL=http://your-cluster:9200 opensearch-monitor
```

## Environment variables

| Variable | Required | Description |
|---|---|---|
| `PORT` | no | Port the server listens on (default `3000`) |
| `OPENSEARCH_URL` | yes | Base URL of the OpenSearch cluster, e.g. `http://opensearch:9200` |
| `OPENSEARCH_USER` | no | Basic auth username, if the cluster requires it |
| `OPENSEARCH_PASSWORD` | no | Basic auth password (only sent if `OPENSEARCH_USER` is set) |

## Prebuilt image (GitHub Container Registry)

A [workflow](../.github/workflows/opensearch-monitor-docker-build.yml) builds and
pushes this image to GHCR on every push to `main`/`dev` that touches
`opensearch-monitor/**`, plus PRs and manual `workflow_dispatch` runs. Pull and
run it directly, no local build needed:

```bash
docker pull ghcr.io/mahieddine-ichir/kockpit/opensearch-monitor:latest
docker run -p 3000:3000 -e OPENSEARCH_URL=http://your-cluster:9200 ghcr.io/mahieddine-ichir/kockpit/opensearch-monitor:latest
```

Other useful tags: `main`, `dev` (latest build on each branch), and
`<branch>-<sha>` for a specific commit. The package is private by default under
GHCR's permissions — make it public (or configure a registry pull secret on
your cloud) if the deploy target needs anonymous pulls.

## Deploying to AWS (ECS or EC2)

The image is a single self-contained container exposing port 3000 — no separate
frontend/backend deployment needed. Either use the prebuilt GHCR image above, or
build your own and push to ECR:

1. Build and push to ECR:
   ```bash
   aws ecr get-login-password --region <region> | docker login --username AWS --password-stdin <account>.dkr.ecr.<region>.amazonaws.com
   docker build -t opensearch-monitor .
   docker tag opensearch-monitor:latest <account>.dkr.ecr.<region>.amazonaws.com/opensearch-monitor:latest
   docker push <account>.dkr.ecr.<region>.amazonaws.com/opensearch-monitor:latest
   ```
2. **ECS (Fargate or EC2 launch type):** create a task definition with one
   container using that image, container port `3000`, and set `OPENSEARCH_URL`
   (plus `OPENSEARCH_USER`/`OPENSEARCH_PASSWORD` if needed) as task environment
   variables or via Secrets Manager. Put the service behind an ALB target group
   health-checking `GET /api/health`. The task's security group needs network
   access to the OpenSearch domain/cluster on port 9200 (or 443 for AWS managed
   OpenSearch with a load balancer in front).
3. **Plain EC2:** install Docker, then `docker run -d --restart unless-stopped -p 3000:3000 -e OPENSEARCH_URL=... <image>`. Put it behind whatever reverse proxy/ALB you already use.

## Notes / limitations

- No authentication on the app itself — put it behind your existing network
  perimeter, VPN, or ALB auth if it needs to be restricted.
- AWS SigV4-signed requests (for AWS OpenSearch Service domains with fine-grained
  access control enabled) aren't implemented — only anonymous or basic auth. Add
  a signing step in `backend/src/index.js` if you need it later.
- The pie chart caps at 7 individual node slices + an "Other" bucket for larger
  clusters (readability); the table below always shows every row.
