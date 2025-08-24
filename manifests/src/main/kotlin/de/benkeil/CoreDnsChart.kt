package de.benkeil

import org.cdk8s.Chart
import org.cdk8s.ChartProps
import org.cdk8s.Size
import org.cdk8s.plus32.*
import software.constructs.Construct

private const val app = "coredns"

class CoreDnsChart(scope: Construct, env: Environment) :
    Chart(
        scope,
        app,
        ChartProps.builder().namespace("home").disableResourceNameHashes(true).build(),
    ) {
  init {
    val corefile =
        ConfigMap.Builder.create(this, "application")
            .name(app)
            .immutable(true)
            .data(
                mapOf(
                    "Corefile" to readResourceAsString("/Corefile"),
                ))
            .build()

    val deployment =
        Deployment.Builder.create(this, "deployment")
            .name(app)
            .replicas(1)
            .containers(
                listOf(
                    ContainerProps.builder()
                        .name("app")
                        .image("coredns/coredns:latest")
                        .ports(
                            listOf(
                                ContainerPort.builder().number(53).protocol(Protocol.TCP).build(),
                                ContainerPort.builder().number(53).protocol(Protocol.UDP).build(),
                                ContainerPort.builder().number(8080).protocol(Protocol.TCP).build(),
                            ))
                        .args(listOf("-conf", "/etc/coredns/Corefile", "-dns.port", "53"))
                        .volumeMounts(
                            listOf(
                                VolumeMount.builder()
                                    .path("/etc/coredns/")
                                    .readOnly(true)
                                    .volume(Volume.fromConfigMap(this, "config", corefile))
                                    .build()))
                        .resources(
                            ContainerResources.builder()
                                .cpu(CpuResources.builder().limit(Cpu.units(1)).build())
                                .memory(
                                    MemoryResources.builder().limit(Size.mebibytes(256)).build())
                                .build())
                        .securityContext(
                            ContainerSecurityContextProps.builder()
                                .ensureNonRoot(false)
                                .readOnlyRootFilesystem(false)
                                .build())
                        .build(),
                ))
            .build()

    deployment.exposeViaService(
        DeploymentExposeViaServiceOptions.builder()
            .name(app)
            .ports(
                listOf(
                    ServicePort.builder()
                        .name("8080-tcp")
                        .port(8080)
                        .protocol(Protocol.TCP)
                        .build(),
                    ServicePort.builder().name("53-tcp").port(53).protocol(Protocol.TCP).build(),
                    ServicePort.builder().name("53-udp").port(53).protocol(Protocol.UDP).build(),
                ))
            .serviceType(ServiceType.LOAD_BALANCER)
            .build())
  }
}
