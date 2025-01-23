set positional-arguments := true
set ignore-comments := true

default:
    just --list

docker: docker-build docker-publish

docker-build target='build-local':
    docker buildx bake {{ target }} --progress plain

_docker_login:
    echo $GITHUB_TOKEN | docker login ghcr.io -u benkeil --password-stdin

docker-publish target='build-local': _docker_login
    TAG=1.0.0 docker buildx bake --push {{ target }}

kubernetes: kubernetes-build kubernetes-deploy

[working-directory('manifests')]
kubernetes-build:
    cdk8s synth

[working-directory('manifests')]
kubernetes-deploy:
    kubectl apply -f dist/*

[confirm]
[working-directory('manifests')]
kubernetes-destroy:
    kubectl delete -f dist/*

[working-directory('infrastructure')]
cdktf-deploy:
    cdktf deploy application --auto-approve
