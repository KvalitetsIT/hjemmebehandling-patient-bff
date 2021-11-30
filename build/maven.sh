#!/bin/sh

apt-get update
apt-get install -y docker.io

SRC_FOLDER=src

if [ -d $SRC_FOLDER ]; then
  cd $SRC_FOLDER

  # Build the bff service
  mvn clean install

  # Start the hapi server

  docker rm hapi-server
  docker run -d --volumes-from maven-builder -e hapi.fhir.allow_external_references=true -e hapi.fhir.expunge_enabled=true -e hapi.fhir.reuse_cached_search_results_millis=1000 --network rim --name hapi-server hapiproject/hapi:latest

  # Initialize the server with data
  docker run --volumes-from maven-builder --network rim -e data_dir='/src/compose/hapi-server-initializer' alpine:3.11.5 /src/compose/hapi-server-initializer/init.sh

  # Start the bff service

  docker rm patient-bff
  docker run -d --network rim --name patient-bff -p 8080:8080 --volumes-from maven-builder kvalitetsit/hjemmebehandling-patient-bff:latest

  # Wait for it to be ready
  echo 'waiting for bff to be ready ...'
  curl -o /dev/null --retry 5 --retry-max-time 40 --retry-connrefused http://patient-bff:8080

  # Run the integration test
  cd integrationtest
  mvn verify -Pintegration-test -Dpatient-bff-host=patient-bff

  # Save the exit code, stop containers
  exit_code=$?
  echo 'Exit code: '$exit_code

  docker stop patient-bff
  docker stop hapi-server

  exit $exit_code
else
  echo "$SRC_FOLDER folder not found."
  exit 1
fi

