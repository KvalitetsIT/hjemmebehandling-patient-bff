#!/bin/sh

# Build resource container and start it
docker build -t resources -f ./integrationtest/docker/Dockerfile-resources --no-cache ./integrationtest/docker
docker run -d --name hjemmebehandling-patient-bff-resources resources

# Build inside docker container
docker run -v /var/run/docker.sock:/var/run/docker.sock  -v $HOME/.docker/config.json:/root/.docker/config.json -v $(pwd):/src -v $HOME/.m2:/root/.m2 --volumes-from hjemmebehandling-patient-bff-resources  maven:3-jdk-11 /src/build/maven.sh
