# Local Kubernetes Cluster Setup

This repository provisions the Kubernetes nodes and cluster using Ansible:
host configuration, CRI-O, kubeadm, control planes, workers and Calico CNI.
Kubernetes cluster DNS is provided by kubeadm's CoreDNS addon.

Argo CD, External Secrets/SSM, MetalLB, Traefik, registry credentials and
application configuration live in
[`homelab-bootstrap`](https://github.com/benkeil/homelab-bootstrap)
(`../homelab-bootstrap` locally). Platform manifests are maintained there as
YAML, Kustomize and Helm values, without Kotlin/CDK8s or Gradle.

## Provisioning

Requirements: Ansible, SSH access to the hosts in `ansible/hosts.ini`, and `just`.
Review the inventory, host/group variables and enabled roles before running.
In particular, the control-plane playbook currently enables only Calico;
enable its `kubernetes` and `kubernetes-control-plane` roles when provisioning
a new control-plane host.

```bash
just infrastructure
```

Or run individual playbooks:

```bash
just infrastructure-control-planes
just infrastructure-workers
```

Calico is installed by the control-plane playbook using
`ansible/roles/calico/templates/calico.yaml`.
Certificate material used by kubeadm remains under the Ansible control-plane
role. `just certificates` generates new material; do not run it against an
existing cluster unless intentionally rotating its keys.

Copy the kubeconfig after setup:

```bash
just copy-kube-config
```

Then continue with the bootstrap instructions in `homelab-bootstrap`.
There are no application deployment commands in this repository.
