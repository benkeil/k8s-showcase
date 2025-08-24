package de.benkeil

import imports.io.traefik.IngressRoute
import imports.io.traefik.IngressRouteSpec
import imports.io.traefik.IngressRouteSpecRoutes
import imports.io.traefik.IngressRouteSpecRoutesKind
import imports.io.traefik.IngressRouteSpecRoutesServices
import imports.io.traefik.IngressRouteSpecRoutesServicesPort
import org.cdk8s.ApiObjectMetadata
import org.cdk8s.Chart
import org.cdk8s.ChartProps
import org.cdk8s.Size
import org.cdk8s.plus32.*
import org.cdk8s.plus32.k8s.HostPathVolumeSource
import org.cdk8s.plus32.k8s.KubePersistentVolume
import org.cdk8s.plus32.k8s.PersistentVolumeSpec
import org.cdk8s.plus32.k8s.Quantity
import software.constructs.Construct

private const val app = "adguardhome"

class AdguardHomeChart(scope: Construct, env: Environment) :
    Chart(
        scope,
        app,
        ChartProps.builder().namespace("home").disableResourceNameHashes(true).build(),
    ) {
  init {
    val configFile =
        ConfigMap.Builder.create(this, "application")
            .name(app)
            .data(
                mapOf(
                    "AdGuardHome.yaml" to readResourceAsString("/AdGuardHome.yaml"),
                ))
            .build()

    val volume =
        KubePersistentVolume.Builder.create(this, "pv")
            .name(app)
            .spec(
                PersistentVolumeSpec.builder()
                    .capacity(mapOf("storage" to Quantity.fromString("1Gi")))
                    .accessModes(listOf("ReadWriteOnce"))
                    .persistentVolumeReclaimPolicy("Retain")
                    .hostPath(HostPathVolumeSource.builder().path("/mnt/kubernetes/$app").build())
                    .build(),
            )
            .build()
    val pvc =
        PersistentVolumeClaim.Builder.create(this, "pvc")
            .name(app)
            .volume(PersistentVolume.fromPersistentVolumeName(this, "from-data", volume.name))
            .accessModes(listOf(PersistentVolumeAccessMode.READ_WRITE_ONCE))
            .storage(Size.gibibytes(1))
            .build()

    val deployment =
        Deployment.Builder.create(this, "deployment")
            .name(app)
            .replicas(1)
            .dockerRegistryAuth(Secret.fromSecretName(this, "docker", "docker"))
            .containers(
                listOf(
                    ContainerProps.builder()
                        .name("app")
                        .image("adguard/adguardhome:latest")
                        .ports(
                            listOf(
                                ContainerPort.builder().number(80).protocol(Protocol.TCP).build(),
                                ContainerPort.builder().number(5353).protocol(Protocol.TCP).build(),
                                ContainerPort.builder().number(5353).protocol(Protocol.UDP).build(),
                                ContainerPort.builder().number(67).protocol(Protocol.UDP).build(),
                                ContainerPort.builder().number(68).protocol(Protocol.UDP).build(),
                                ContainerPort.builder().number(443).protocol(Protocol.TCP).build(),
                                ContainerPort.builder().number(443).protocol(Protocol.UDP).build(),
                                ContainerPort.builder().number(3000).protocol(Protocol.TCP).build(),
                                ContainerPort.builder().number(853).protocol(Protocol.TCP).build(),
                                ContainerPort.builder().number(853).protocol(Protocol.UDP).build(),
                                ContainerPort.builder().number(784).protocol(Protocol.UDP).build(),
                                ContainerPort.builder().number(8853).protocol(Protocol.UDP).build(),
                                ContainerPort.builder().number(5443).protocol(Protocol.TCP).build(),
                                ContainerPort.builder().number(5443).protocol(Protocol.UDP).build(),
                            ))
                        .args(
                            listOf(
                                "--work-dir",
                                "/usr/adguardhome/work",
                                "--config",
                                "/usr/adguardhome/conf/AdGuardHome.yaml",
                            ))
                        .volumeMounts(
                            listOf(
                                // VolumeMount.builder()
                                //     .path("/opt/adguardhome/conf")
                                //     .readOnly(true)
                                //     .volume(Volume.fromConfigMap(this, "config", configFile))
                                //     .build(),
                                VolumeMount.builder()
                                    .path("/usr/adguardhome")
                                    .readOnly(false)
                                    .volume(Volume.fromPersistentVolumeClaim(this, "data", pvc))
                                    .build(),
                            ))
                        .resources(
                            ContainerResources.builder()
                                .cpu(CpuResources.builder().limit(Cpu.units(1)).build())
                                .memory(
                                    MemoryResources.builder().limit(Size.mebibytes(512)).build())
                                .build())
                        .securityContext(
                            ContainerSecurityContextProps.builder()
                                .ensureNonRoot(false)
                                .readOnlyRootFilesystem(false)
                                .build())
                        .build(),
                ))
            .build()

    val service =
        deployment.exposeViaService(
            DeploymentExposeViaServiceOptions.builder()
                .name(app)
                .ports(
                    listOf(
                        ServicePort.builder().name("http").port(80).protocol(Protocol.TCP).build(),
                        ServicePort.builder()
                            .name("install")
                            .port(3000)
                            .protocol(Protocol.TCP)
                            .build(),
                    ))
                .serviceType(ServiceType.CLUSTER_IP)
                .build())

    Service.Builder.create(this, "service-dns")
        .metadata(
            ApiObjectMetadata.builder()
                .name("$app-dns")
                .annotations(mapOf("metallb.io/loadBalancerIPs" to "192.168.20.101"))
                .build())
        .ports(
            listOf(
                ServicePort.builder()
                    .name("53-tcp")
                    .port(53)
                    .targetPort(5353)
                    .protocol(Protocol.TCP)
                    .build(),
                ServicePort.builder()
                    .name("53-udp")
                    .port(53)
                    .targetPort(5353)
                    .protocol(Protocol.UDP)
                    .build(),
            ))
        .type(ServiceType.LOAD_BALANCER)
        .build()

    IngressRoute.Builder.create(this, "ingressroute")
        .name(app)
        .spec(
            IngressRouteSpec.builder()
                .entryPoints(listOf("websecure"))
                .routes(
                    listOf(
                        IngressRouteSpecRoutes.builder()
                            .kind(IngressRouteSpecRoutesKind.RULE)
                            .match("Host(`$app.${env.domain}`)")
                            .services(
                                listOf(
                                    IngressRouteSpecRoutesServices.builder()
                                        .name(service.name)
                                        .port(IngressRouteSpecRoutesServicesPort.fromNumber(80))
                                        .build(),
                                ),
                            )
                            .build(),
                        IngressRouteSpecRoutes.builder()
                            .kind(IngressRouteSpecRoutesKind.RULE)
                            .match("Host(`$app.${env.domain}`) && (PathPrefix(`/install`))")
                            .services(
                                listOf(
                                    IngressRouteSpecRoutesServices.builder()
                                        .name(service.name)
                                        .port(IngressRouteSpecRoutesServicesPort.fromNumber(3000))
                                        .build(),
                                ),
                            )
                            .build(),
                    ),
                )
                .build())
        .build()
  }
}
