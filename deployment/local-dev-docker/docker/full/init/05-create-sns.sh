#!/bin/bash

export AWS_DEFAULT_REGION=eu-west-1

echo "==================="
echo "configuring SNS alerting topic"
awslocal sns create-topic --name sns-topic-alerting
