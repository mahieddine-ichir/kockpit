aws s3 cp wcc-manifest-local.json s3://wcp-manifest-bucket --endpoint-url=http://localhost:4566
aws s3 cp ../manifests/wcc-manifest-local.json s3://wcp-manifest-bucket --endpoint-url=http://localhost:4566
aws s3 cp wcxss-manifest-local.json s3://wcp-manifest-bucket --endpoint-url=http://localhost:4566
aws s3 cp ../manifests/wcxss-manifest-local.json s3://wcp-manifest-bucket --endpoint-url=http://localhost:4566

aws s3 cp ../manifests/wcp-samples-manifest-local.json s3://wcp-manifest-bucket --endpoint-url=http://localhost:4566
aws s3 cp ../manifests/wcp-dynaconfig-manifest-local.json s3://wcp-manifest-bucket --endpoint-url=http://localhost:4566


