#!/bin/bash
set -e

echo "Waiting for OpenSearch to start..."
until curl -s http://localhost:9200 >/dev/null; do
  sleep 5
done

index=

echo "Creating index mapping ..."
curl -X PUT "http://localhost:9200/rcu-audit-data-dev-read"

curl -X PUT "http://localhost:9200/rcu-audit-data-dev-read/_mapping" \
    -H 'Content-Type: application/json' \
    --data-binary @index-template.json

echo "Loading test data into OpenSearch..."
curl -X POST "http://localhost:9200/rcu-audit-data-dev-read/_bulk" \
     -H 'Content-Type: application/json' \
     --data-binary @audits.json

echo "test data loaded successfully"
