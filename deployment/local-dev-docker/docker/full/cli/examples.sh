# DynamoDB
aws --endpoint-url=http://localhost:4566 dynamodb list-tables
aws --endpoint-url=http://localhost:4566 dynamodb scan --table-name SqsDocument

# SQS
aws --endpoint-url=http://localhost:4566 sqs list-queues

# Read SQS
aws --endpoint-url=http://localhost:4566 sqs receive-message --queue-url http://localhost:4566/000000000000/booking-event-notif.fifo  --message-attribute-names All --attribute-names All --max-number-of-messages 10

#Send to SQS
aws --endpoint-url=http://localhost:4566 sqs send-message --queue-url http://localhost:4566/000000000000/insurance-cancellation-notif-dlq --message-body "body-insurance-cancellation-notif-dlq" --message-group-id "message-group-id"  --message-attributes file:///message-attributes.json

#Local docker :
docker ps
docker exec -it {containerId:5739f7061aeb} /bin/bash
awslocal sqs send-message --queue-url http://localhost:4566/000000000000/booking-event-notif-dlq.fifo --message-body "body-booking-event-notif-dlq"  --message-group-id "message-group-id"  --message-attributes file:///docker-entrypoint-initaws.d/message-attributes.json
awslocal sqs send-message --queue-url http://localhost:4566/000000000000/insurance-cancellation-notif-dlq --message-body "body-insurance-cancellation-notif-dlq"  --message-attributes file:///docker-entrypoint-initaws.d/message-attributes.json

#s3
aws s3 ls --endpoint-url=http://localhost:4566
aws s3 ls s3://wcp-manifest-bucket  --endpoint-url=http://localhost:4566
aws s3 cp filetocopy s3://wcp-manifest-bucket --endpoint-url=http://localhost:4566
