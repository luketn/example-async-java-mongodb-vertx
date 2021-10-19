#!/bin/bash

ARCHITECTURE=${1:-"amd64"} # / arm64
TAG_NAME=${2:-"example-async-mongodb"}

echo "Building local image tagged '${TAG_NAME}' with architecture '${ARCHITECTURE}'!"

docker build --platform "linux/${ARCHITECTURE}" -t "${TAG_NAME}" -f "docker-${ARCHITECTURE}.Dockerfile" .
