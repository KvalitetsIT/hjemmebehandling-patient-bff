#!/bin/bash

if docker pull kvalitetsit/hjemmebehandling-patient-bff-documentation:latest; then
    echo "Copy from old documentation image."
    docker cp $(docker create kvalitetsit/hjemmebehandling-patient-bff-documentation:latest):/usr/share/nginx/html target/old
fi
