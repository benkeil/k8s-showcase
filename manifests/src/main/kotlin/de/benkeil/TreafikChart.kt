package de.benkeil

import imports.io.traefik.IngressRoute
import imports.io.traefik.IngressRouteSpec
import imports.io.traefik.IngressRouteSpecRoutes
import imports.io.traefik.IngressRouteSpecRoutesKind
import imports.io.traefik.IngressRouteSpecRoutesMiddlewares
import imports.io.traefik.IngressRouteSpecRoutesServices
import imports.io.traefik.IngressRouteSpecRoutesServicesKind
import imports.io.traefik.Middleware
import imports.io.traefik.MiddlewareSpec
import imports.io.traefik.MiddlewareSpecBasicAuth
import org.cdk8s.Chart
import org.cdk8s.ChartProps
import org.cdk8s.Helm
import org.cdk8s.plus32.Namespace
import org.cdk8s.plus32.Secret
import org.cdk8s.plus32.k8s.HostPathVolumeSource
import org.cdk8s.plus32.k8s.KubePersistentVolume
import org.cdk8s.plus32.k8s.PersistentVolumeSpec
import org.cdk8s.plus32.k8s.Quantity
import software.constructs.Construct

private const val app = "traefik"

class TreafikChart(scope: Construct, env: Environment) :
    Chart(
        scope,
        app,
        ChartProps.builder().namespace(app).disableResourceNameHashes(true).build(),
    ) {
  init {
    Namespace.Builder.create(this, app).name(app).build()

    Helm.Builder.create(this, "crds")
        .chart("traefik-crds")
        .version("1.8.1")
        .namespace(app)
        .repo("https://helm.traefik.io/traefik")
        .build()

    Secret.Builder.create(this, "aws-credentials")
        .name("aws-credentials")
        .stringData(
            mapOf(
                "AWS_ACCESS_KEY_ID" to env.awsAccessKeyId,
                "AWS_SECRET_ACCESS_KEY" to env.awsSecretAccessKey,
                "AWS_REGION" to env.awsRegion,
                "AWS_HOSTED_ZONE_ID" to env.awsHostedZoneId,
            ))
        .build()

    val dashboardAuthSecret =
        Secret.Builder.create(this, "secret-dashboard-auth")
            .name("traefik-dashboard-auth")
            // htpasswd -nb USER PASSWORD | openssl base64
            .stringData(
                mapOf(
                    "users" to "ben:$2y$05\$Fo5Jse4cPuIhwBe7HQoDgu3Gxc.BIrNMYQC3ky8pZgovI5XHkxV1m",
                ))
            .build()

    val basicAuthMiddleware =
        Middleware.Builder.create(this, "dashboard-auth")
            .name("traefik-dashboard-basicauth")
            .spec(
                MiddlewareSpec.builder()
                    .basicAuth(
                        MiddlewareSpecBasicAuth.builder().secret(dashboardAuthSecret.name).build())
                    .build())
            .build()

    IngressRoute.Builder.create(this, "traefik-dashboard")
        .name("traefik-dashboard")
        .spec(
            IngressRouteSpec.builder()
                .entryPoints(listOf("websecure"))
                .routes(
                    listOf(
                        IngressRouteSpecRoutes.builder()
                            .kind(IngressRouteSpecRoutesKind.RULE)
                            .match("Host(`traefik-dashboard.${env.domain}`)")
                            .middlewares(
                                listOf(
                                    IngressRouteSpecRoutesMiddlewares.builder()
                                        .name(basicAuthMiddleware.name)
                                        .namespace(app)
                                        .build(),
                                ))
                            .services(
                                listOf(
                                    IngressRouteSpecRoutesServices.builder()
                                        .name("api@internal")
                                        .kind(IngressRouteSpecRoutesServicesKind.TRAEFIK_SERVICE)
                                        .build(),
                                ),
                            )
                            .build(),
                    ),
                )
                .build())
        .build()

    val volume =
        KubePersistentVolume.Builder.create(this, "pv-traefik")
            .name(app)
            .spec(
                PersistentVolumeSpec.builder()
                    .capacity(mapOf("storage" to Quantity.fromString("64Mi")))
                    .accessModes(listOf("ReadWriteOnce"))
                    .persistentVolumeReclaimPolicy("Retain")
                    .hostPath(
                        HostPathVolumeSource.builder().path("/mnt/kubernetes/traefik").build())
                    .build(),
            )
            .build()

    val certPath = "/certs"
    Helm.Builder.create(this, "helm")
        .chart("traefik")
        .version("36.1.0")
        .namespace(app)
        .repo("https://helm.traefik.io/traefik")
        .values(
            mapOf(
                "ingressClass" to mapOf("name" to "traefik"),
                "env" to
                    listOf(
                        valueFromSecret("AWS_ACCESS_KEY_ID"),
                        valueFromSecret("AWS_SECRET_ACCESS_KEY"),
                        valueFromSecret("AWS_REGION"),
                        valueFromSecret("AWS_HOSTED_ZONE_ID"),
                    ),
                "additionalArguments" to
                    buildList {
                      // tls
                      add("--entrypoints.websecure.http.tls.certresolver=letsencrypt")
                      add("--entrypoints.websecure.http.tls.domains[0].main=${env.domain}")
                      add("--entrypoints.websecure.http.tls.domains[0].sans=*.${env.domain}")
                      // cert resolver
                      add("--certificatesresolvers.letsencrypt.acme.dnschallenge.provider=route53")
                      add("--certificatesresolvers.letsencrypt.acme.email=${env.email}")
                      if (env.letsencryptUseStaging) {
                        add(
                            "--certificatesresolvers.letsencrypt.acme.storage=$certPath/acme-staging.json")
                        add(
                            "--certificatesresolvers.letsencrypt.acme.caserver=https://acme-staging-v02.api.letsencrypt.org/directory")
                      } else {
                        add("--certificatesresolvers.letsencrypt.acme.storage=$certPath/acme.json")
                      }
                    },
                "ingressRoute" to mapOf("dashboard" to mapOf("enabled" to true)),
                "logs" to mapOf("general" to mapOf("level" to "INFO")),
                "deployment" to
                    mapOf(
                        "initContainers" to
                            listOf(
                                mapOf(
                                    "name" to "volume-setup",
                                    "image" to "busybox:latest",
                                    "command" to
                                        listOf("sh", "-c", "chown -R 65532:65532 /mnt/data"),
                                    "securityContext" to
                                        mapOf(
                                            "runAsUser" to 0,
                                            "runAsNonRoot" to false,
                                        ),
                                    "volumeMounts" to
                                        listOf(
                                            mapOf(
                                                "name" to "data",
                                                "mountPath" to "/mnt/data",
                                            )),
                                ),
                            )),
                "ports" to
                    mapOf(
                        "web" to
                            mapOf(
                                "redirections" to
                                    mapOf(
                                        "entryPoint" to
                                            mapOf(
                                                "to" to "websecure",
                                                "scheme" to "https",
                                                "permanent" to true,
                                            )))),
                "persistence" to
                    mapOf(
                        "enabled" to true,
                        "path" to certPath,
                        "size" to "64Mi",
                        "volumeName" to volume.name,
                    ),
            ))
        .build()
  }
}

fun valueFromSecret(
    key: String,
    secretName: String = "aws-credentials",
): Map<String, Any> =
    mapOf(
        "name" to key,
        "valueFrom" to
            mapOf(
                "secretKeyRef" to
                    mapOf(
                        "key" to key,
                        "name" to secretName,
                    ),
            ),
    )
