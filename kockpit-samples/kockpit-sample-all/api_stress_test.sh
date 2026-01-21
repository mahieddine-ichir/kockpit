 #ab -n 10 -c 1 "http://localhost:8082/sample-app/api/sayHellox/michir"

for i in {1..300}
do
  curl -sX GET "http://localhost:8082/sample-app/api/sayHello/michir_${i}" >> /dev/null
  curl -sX GET "http://localhost:8082/sample-app/api/facts" >> /dev/null
  curl -sX DELETE "http://localhost:8082/sample-app/api/sayHello" >> /dev/null
  curl -H 'Content-Type: application/json' -sX POST "http://localhost:8082/sample-app/api/createMessage" -d'{}' >> /dev/null
  printf '.'
done
printf '\n'
