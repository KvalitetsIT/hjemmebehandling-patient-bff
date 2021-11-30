#!/bin/sh

echo "${GITHUB_REPOSITORY}"
echo "${DOCKER_SERVICE}"
if [ "${GITHUB_REPOSITORY}" != "KvalitetsIT/hjemmebehandling-patient-bff" ] && [ "${DOCKER_SERVICE}" = "kvalitetsit/hjemmebehandling-patient-bff" ]; then
  echo "Please run setup.sh REPOSITORY_NAME"
  exit 1
fi
