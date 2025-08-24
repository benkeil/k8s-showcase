package de.benkeil

import org.cdk8s.Chart
import org.cdk8s.ChartProps
import org.cdk8s.plus32.Namespace
import org.cdk8s.plus32.Secret
import software.constructs.Construct
import java.util.Base64

private const val app = "home"

class HomeChart(scope: Construct, env: Environment) :
    Chart(
        scope,
        app,
        ChartProps.builder().namespace(app).disableResourceNameHashes(true).build(),
    ) {
  init {
    Namespace.Builder.create(this, app).name(app).build()

      val auth =
          Base64.getEncoder()
              .encodeToString("${env.dockerUsername}:${env.dockerPassword}".toByteArray())
      val dockerSecret =
          Secret.Builder.create(this, "docker")
              .name("docker")
              .type("kubernetes.io/dockerconfigjson")
              .stringData(
                  mapOf(
                      ".dockerconfigjson" to
                              """
                        {
                            "auths": {
                                "https://index.docker.io/v1/": {
                                    "username":"${env.dockerUsername}",
                                    "password":"${env.dockerPassword}",
                                    "email":"${env.dockerUsername}",
                                    "auth":"$auth"
                                }
                            }
                        }
                        """
                                  .trimIndent(),
                  ),
              )
              .build()
  }
}
