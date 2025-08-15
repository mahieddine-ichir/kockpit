#!/bin/bash

export AWS_DEFAULT_REGION=eu-west-1

echo "==================="
echo "configuring Dynamo for audit registry"
awslocal dynamodb create-table --table-name KEngineRegistryDocument --attribute-definitions AttributeName=id,AttributeType=S --key-schema AttributeName=id,KeyType=HASH --provisioned-throughput ReadCapacityUnits=1,WriteCapacityUnits=1
