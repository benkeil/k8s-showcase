# kubeadm configuration

See <https://kubernetes.io/docs/reference/config-api/kubeadm-config.v1beta4/#kubeadm-k8s-io-v1beta4-NodeRegistrationOptions>

## Join a worker

| Parameter                    | Value                                                              |
|------------------------------|--------------------------------------------------------------------|
| DISCOVERY_TOKEN_CA_CERT_HASH | `c678d6873e76ad9da70da4549dd3ceb33094e2703243ebc21929a004a9fb5ebb` |

```bash
kubeadm join --token <JOIN_TOKEN> kube-control-plane:6443 --discovery-token-ca-cert-hash sha256:<DISCOVERY_TOKEN_CA_CERT_HASH>
```
