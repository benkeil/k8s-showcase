package de.benkeil

import imports.io.metallb.IpAddressPool
import imports.io.metallb.IpAddressPoolSpec
import imports.io.metallb.IpAddressPoolSpecServiceAllocation
import imports.io.metallb.L2Advertisement
import imports.io.metallb.L2AdvertisementSpec
import org.cdk8s.ApiObjectMetadata
import org.cdk8s.Chart
import org.cdk8s.ChartProps
import software.constructs.Construct

private const val app = "metallb"

class MetallbChart(scope: Construct, env: Environment) :
    Chart(
        scope,
        app,
        ChartProps.builder().namespace(app).disableResourceNameHashes(true).build(),
    ) {
  init {
    IpAddressPool.Builder.create(this, "IpAddressPool-load-balancer")
        .metadata(ApiObjectMetadata.builder().name("load-balancer").namespace(app).build())
        .spec(
            IpAddressPoolSpec.builder()
                .addresses(listOf("192.168.20.100/32"))
                .serviceAllocation(
                    IpAddressPoolSpecServiceAllocation.builder()
                        .namespaces(listOf("traefik"))
                        .priority(1)
                        .build())
                .build())
        .build()
        .also { pool ->
          L2Advertisement.Builder.create(this, "L2Advertisement-load-balancer")
              .metadata(ApiObjectMetadata.builder().name("load-balancer").namespace(app).build())
              .spec(L2AdvertisementSpec.builder().ipAddressPools(listOf(pool.name)).build())
              .build()
        }

    IpAddressPool.Builder.create(this, "IpAddressPool-internal")
        .metadata(ApiObjectMetadata.builder().name("internal").namespace(app).build())
        .spec(
            IpAddressPoolSpec.builder().addresses(listOf("192.168.20.101-192.168.20.199")).build())
        .build()
        .also { pool ->
          L2Advertisement.Builder.create(this, "L2Advertisement-internal")
              .metadata(ApiObjectMetadata.builder().name("internal").namespace(app).build())
              .spec(L2AdvertisementSpec.builder().ipAddressPools(listOf(pool.name)).build())
              .build()
        }
  }
}
