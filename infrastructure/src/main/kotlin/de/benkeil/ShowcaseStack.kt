package de.benkeil

import com.hashicorp.cdktf.TerraformStack
import com.hashicorp.cdktf.providers.kubernetes.namespace.Namespace
import com.hashicorp.cdktf.providers.kubernetes.namespace.NamespaceMetadata
import com.hashicorp.cdktf.providers.kubernetes.provider.KubernetesProvider
import com.hashicorp.cdktf.providers.kubernetes.secret.Secret
import com.hashicorp.cdktf.providers.kubernetes.secret.SecretMetadata
import software.constructs.Construct

class ShowcaseStack(scope: Construct, env: Environment) : TerraformStack(scope, "application") {
  init {
    KubernetesProvider.Builder.create(this, "kubernetes")
        .configPath("~/.kube/berry.config")
        .configContext("kind-kind")
        .build()

    Namespace.Builder.create(this, "namespace")
        .metadata(NamespaceMetadata.builder().name("team").build())
        .build()

    Secret.Builder.create(this, "secret")
        .metadata(SecretMetadata.builder().name("showcase").namespace("team").build())
        .data(mapOf("APPLICATION_GREETING" to "ben"))
        .build()
  }
}
