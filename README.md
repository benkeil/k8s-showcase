# Local Kubernetes Cluster Setup

## How to

Install kubernetes

```bash
just infrastructure
```

After first installation, install Calico CNI

```bash
kubectl create -f https://raw.githubusercontent.com/projectcalico/calico/v3.29.2/manifests/tigera-operator.yaml
```

Configure `resources/calico.yaml`

```bash
kubectl create -f resources/calico.yaml
```

## TODO

- make CRI-O a dedicated role: <https://github.com/cri-o/packaging/blob/main/README.md#usage>
