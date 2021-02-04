HASH=$(git rev-parse --short HEAD)
echo "Creating image tagged with quay.io/gnunn/server-native:${HASH}"
podman build -f src/main/docker/Dockerfile.native -t quay.io/gnunn/server-native:${HASH} .
echo "Tagging image as latest"
podman tag quay.io/gnunn/server-native:${HASH} quay.io/gnunn/server-native:latest