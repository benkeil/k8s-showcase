package de.benkeil

import org.cdk8s.ApiObjectMetadata
import org.cdk8s.Chart
import org.cdk8s.ChartProps
import org.cdk8s.Size
import org.cdk8s.plus31.*
import software.constructs.Construct

class ShowcaseChart(scope: Construct, env: Environment) :
    Chart(scope, "showcase", ChartProps.builder().namespace("team").build()) {
  init {
    // Namespace.Builder.create(this, "namespace")
    //     .metadata(ApiObjectMetadata.builder().name("team").build())
    //     .build()

    val secret = Secret.fromSecretName(this, "secret", "showcase")
    // val secret = Secret.Builder.create(this, "secret").type("Opaque")
    //     .stringData(mapOf("APPLICATION_GREETING" to "ben"))
    //     .build()

    val deployment =
        Deployment.Builder.create(this, "deployment")
            .containers(
                listOf(
                    ContainerProps.builder()
                        .name("webserver")
                        .image("ghcr.io/benkeil/k8s-showcase:${env.version}")
                        .portNumber(8080)
                        .envFrom(listOf(Env.fromSecret(secret)))
                        .resources(
                            ContainerResources.builder()
                                .cpu(CpuResources.builder().limit(Cpu.millis(10)).build())
                                .memory(
                                    MemoryResources.builder().limit(Size.mebibytes(100)).build())
                                .build())
                        .build(),
                ))
            .replicas(1)
            .build()

    deployment.exposeViaIngress(
        "/showcase",
        ExposeDeploymentViaIngressOptions.builder().serviceType(ServiceType.NODE_PORT).build())
    // val service =
    //     deployment.exposeViaService(
    //         DeploymentExposeViaServiceOptions.builder()
    //             .ports(listOf(ServicePort.builder().port(80).targetPort(8080).build()))
    //             .build())
    // Ingress.Builder.create(this, "ingress")
    //     .build()
    //     .addRule("/", IngressBackend.fromService(deployment.service), HttpIngressPathType.EXACT)
  }
}
