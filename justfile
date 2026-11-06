set positional-arguments := true
set ignore-comments := true

default:
    just --list

copy-kube-config:
    scp kube-control-plane:/home/ben/.kube/config ~/.kube/kubernetes.local.config

[working-directory('ansible/roles/kubernetes-control-plane/files')]
certificates:
    openssl genrsa -out ca.key 2048
    openssl req -x509 -new -nodes -key ca.key -subj "/CN=kube-control-plane" -days 10000 -out ca.crt
    ssh-keygen -t rsa -b 2048 -f sa-signer.key -m "" -N ""
    ssh-keygen -e -m PKCS8 -f sa-signer.key.pub > sa-signer-pkcs8.pub

infrastructure:
    just ansible-playbook site.yml

infrastructure-control-planes:
    just ansible-playbook kubernetes-control-planes.yml

infrastructure-workers:
    just ansible-playbook kubernetes-workers.yml

[working-directory('ansible')]
ansible-playbook *args='':
    ansible-playbook -i hosts.ini {{ args }}
