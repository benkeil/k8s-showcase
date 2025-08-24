set positional-arguments := true
set ignore-comments := true

default:
    just --list

#####################
# INITIALIZATION
#####################

[working-directory('manifests')]
init:
    cdk8s import

#####################
# COPY CONFIG
#####################

copy-kube-config:
    scp kube-control-plane:/home/ben/.kube/config ~/.kube/kubernetes.local.config

#####################
# MANIFESTS
#####################

[working-directory('manifests')]
artifact:
    cdk8s synth

#####################
# KUBERNETES
#####################

[working-directory('manifests')]
deploy-coredns:
    @just deploy coredns

[working-directory('manifests')]
deploy-home:
    @just deploy home

[working-directory('manifests')]
deploy-metallb-crds:
    @just deploy metallb-crds

[working-directory('manifests')]
deploy-metallb:
    @just deploy metallb

[working-directory('manifests')]
deploy-traefik:
    @just deploy traefik

[working-directory('manifests')]
deploy-adguardhome:
    @just deploy adguardhome

[working-directory('manifests')]
deploy chart:
    kubectl apply -f dist/{{ chart }}.k8s.yaml

#####################
# CERTIFICATES
#####################

[working-directory('ansible/roles/kubernetes-control-plane/files')]
certificates:
    openssl genrsa -out ca.key 2048
    openssl req -x509 -new -nodes -key ca.key -subj "/CN=kube-control-plane" -days 10000 -out ca.crt
    ssh-keygen -t rsa -b 2048 -f sa-signer.key -m pem -N ""
    ssh-keygen -e -m PKCS8 -f sa-signer.key.pub > sa-signer-pkcs8.pub

#####################
# INFRASTRUCTURE
#####################

infrastructure:
    just ansible-playbook site.yml

infrastructure-control-planes:
    just ansible-playbook kubernetes-control-planes.yml

infrastructure-workers:
    just ansible-playbook kubernetes-workers.yml

#####################
# ANSIBLE
#####################

[working-directory('ansible')]
ansible-playbook *args='':
    ansible-playbook -i hosts.ini {{ args }}

#####################
# ENCRYPTION
#####################

encrypt:
    age --encrypt --identity ~/.config/sops/age/key.txt -o manifests/src/main/resources/secret.properties.enc manifests/src/main/resources/secret.properties
