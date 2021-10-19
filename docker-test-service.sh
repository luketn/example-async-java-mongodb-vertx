#!/bin/bash

docker-compose up -d
docker run --network example_async_java_mongodb_vertx appropriate/curl http://service:9090/examples.json | jq . > examples.json
cat examples.json
cat examples.json | jq -r '.[] | select(.name=="Luke") | .age' > age.txt

if [[ "$(cat age.txt)" != "44" ]]; then
  echo "Failed to find expected default data from service output (Luke's age is 44)."
  exit 1
else
  echo "Luke's age successfully returned by service '$(cat age.txt)'!"
fi
