package de.benkeil

import org.cdk8s.Chart
import org.cdk8s.ChartProps
import org.cdk8s.plus32.PersistentVolumeReclaimPolicy
import org.cdk8s.plus32.k8s.KubeStorageClass
import org.cdk8s.plus32.k8s.ObjectMeta
import software.constructs.Construct

private const val app = "storage"

class StorageChart(scope: Construct, env: Environment) :
    Chart(
        scope,
        app,
        ChartProps.builder().namespace(app).disableResourceNameHashes(true).build(),
    ) {
  init {
    KubeStorageClass.Builder.create(this, "standard")
        .metadata(
            ObjectMeta.builder()
                .name("standard")
                .annotations(mapOf("storageclass.kubernetes.io/is-default-class" to "true"))
                .build())
        .mountOptions(listOf("ReadWriteOnce"))
        .reclaimPolicy("Retain")
        .allowVolumeExpansion(true)
        .provisioner("kubernetes.io/no-provisioner")
        .volumeBindingMode("WaitForFirstConsumer")
        .build()
  }
}
