package de.benkeil

import imports.io.traefik.IngressRoute
import imports.io.traefik.Middleware
import java.io.File
import kotlin.io.path.toPath
import org.cdk8s.*
import org.cdk8s.plus32.ConfigMap
import org.cdk8s.plus32.Deployment
import org.cdk8s.plus32.Namespace
import org.cdk8s.plus32.PersistentVolumeClaim
import org.cdk8s.plus32.Secret
import org.cdk8s.plus32.Service
import org.cdk8s.plus32.k8s.KubePersistentVolume
import org.cdk8s.plus32.k8s.ObjectMeta

fun main() {
  val app = App(AppProps.builder().yamlOutputType(YamlOutputType.FILE_PER_CHART).build())
  val env = Environment.fromConfig()
  MetallbCrdsChart(app, env)
  MetallbChart(app, env)
  TreafikChart(app, env)
  HomeChart(app, env)
  AdguardHomeChart(app, env)
  // CoreDnsChart(app, env)
  app.synth()
}

fun Namespace.Builder.name(name: String): Namespace.Builder =
    metadata(ApiObjectMetadata.builder().name(name).namespace(name).build())

fun Secret.Builder.name(name: String): Secret.Builder =
    metadata(ApiObjectMetadata.builder().name(name).build())

fun Middleware.Builder.name(name: String): Middleware.Builder =
    metadata(ApiObjectMetadata.builder().name(name).build())

fun IngressRoute.Builder.name(name: String): IngressRoute.Builder =
    metadata(ApiObjectMetadata.builder().name(name).build())

fun KubePersistentVolume.Builder.name(name: String): KubePersistentVolume.Builder =
    metadata(ObjectMeta.builder().name(name).build())

fun PersistentVolumeClaim.Builder.name(name: String): PersistentVolumeClaim.Builder =
    metadata(ApiObjectMetadata.builder().name(name).build())

fun Deployment.Builder.name(name: String): Deployment.Builder =
    metadata(ApiObjectMetadata.builder().name(name).build())

fun ConfigMap.Builder.name(name: String): ConfigMap.Builder =
    metadata(ApiObjectMetadata.builder().name(name).build())

fun Service.Builder.name(name: String): Service.Builder =
    metadata(ApiObjectMetadata.builder().name(name).build())

fun readResourceAsString(path: String): String =
    object {}.javaClass.getResource(path)?.readText() ?: error("Resource not found: $path")

fun readResourceAsFile(path: String): File =
    object {}.javaClass.getResource(path)?.toURI()?.toPath()?.toFile()
        ?: error("Resource not found: $path")
