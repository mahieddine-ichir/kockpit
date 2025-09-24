#!/bin/bash
set -e

echo "Waiting for OpenSearch to start..."
until curl -s http://opensearch:9200 >/dev/null; do
  sleep 5
done

echo "Loading test data into OpenSearch..."
curl -X POST "http://opensearch:9200/rcu-dev-audit_data-read/_bulk" \
     -H 'Content-Type: application/json' \
     --data-binary @/init-data/audits.json

echo "test data loaded successfully"
