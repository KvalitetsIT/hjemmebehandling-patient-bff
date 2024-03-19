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
  docker run -d --volumes-from maven-builder -e hapi.fhir.allow_external_references=true -e hapi.fhir.expunge_enabled=true -e hapi.fhir.reuse_cached_search_results_millis=1000 --network rim --name hapi-server hapiproject/hapi:v5.6.0

  # Initialize the server with data
  docker run --network rim -e init_test_data=true -e init_questionnaire_and_plandefinition_infektionsmedicinsk=true kvalitetsit/hjemmebehandling-data-initializer:latest

  # Start the bff service

  docker rm patient-bff
  docker run -d --network rim --name patient-bff -e user.context.handler=MOCK -e user.mock.context.cpr=0101010101 -p 8080:8080 --volumes-from maven-builder kvalitetsit/hjemmebehandling-patient-bff:latest

  # Wait for it to be ready
  echo 'waiting for bff to be ready ...'
  curl -o /dev/null --retry 10 --retry-delay 10 --retry-connrefused http://patient-bff:8080

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

