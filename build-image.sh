HASH=$(git rev-parse --short HEAD)
echo "Creating image tagged with quay.io/hupiper/server-native:${HASH}"
podman build -f src/main/docker/Dockerfile.native -t quay.io/hupiper/server-native:${HASH} .
echo "Tagging image as latest"
podman tag quay.io/hupiper/server-native:${HASH} quay.io/hupiper/server-native:latest