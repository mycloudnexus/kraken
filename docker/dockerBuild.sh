#!/bin/sh
# execute this script from the root of the project

# Build the docker image for the api-hub
docker buildx build -t cloudnexusopsdockerhub/kraken-app-hub:latest -f ./docker/app-hub/Dockerfile .

# Build the docker image for the api-agent
docker buildx build -t cloudnexusopsdockerhub/kraken-app-agent:latest -f ./docker/app-agent/Dockerfile .

# Build the docker image for the api-controller
docker buildx build -t cloudnexusopsdockerhub/kraken-app-controller:latest -f ./docker/app-controller/Dockerfile .


# Build the docker image for the portal-app
docker buildx build -t cloudnexusopsdockerhub/kraken-app-portal:latest -f ./docker/app-portal/Dockerfile .