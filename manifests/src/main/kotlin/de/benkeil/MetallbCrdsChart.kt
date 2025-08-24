package de.benkeil

import org.cdk8s.ApiObjectMetadata
import org.cdk8s.Chart
import org.cdk8s.ChartProps
import org.cdk8s.Helm
import org.cdk8s.plus32.Namespace
import software.constructs.Construct

private const val app = "metallb"

class MetallbCrdsChart(scope: Construct, env: Environment) :
    Chart(
        scope,
        "$app-crds",
        ChartProps.builder().namespace(app).disableResourceNameHashes(true).build(),
    ) {
  init {
    Namespace.Builder.create(this, app)
        .metadata(
            ApiObjectMetadata.builder()
                .name(app)
                .namespace(app)
                .labels(
                    mapOf(
                        "pod-security.kubernetes.io/enforce" to "privileged",
                        "pod-security.kubernetes.io/audit" to "privileged",
                        "pod-security.kubernetes.io/warn" to "privileged",
                    ))
                .build())
        .build()

    Helm.Builder.create(this, "helm")
        .chart("metallb")
        .version("0.15.2")
        .namespace(app)
        .repo("https://metallb.github.io/metallb")
        .build()
  }
}
