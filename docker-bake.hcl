variable "DOCKER_REGISTRY" {
  default = "ghcr.io"
}

variable "TAG" {
  default = "dev"
}

target "docker-metadata-action" {}

target "build" {
  inherits = ["docker-metadata-action"]
  context    = "./"
  dockerfile = "code/application/Dockerfile"
  platforms = [
    "linux/arm64"
  ]
}

target "build-local" {
  inherits = ["build"]
  tags = ["${DOCKER_REGISTRY}/benkeil/k8s-showcase:${TAG}"]
}
