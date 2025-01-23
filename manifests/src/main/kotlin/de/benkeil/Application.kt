package de.benkeil

import de.benkeil.model.Stage
import org.cdk8s.App
import org.cdk8s.AppProps
import org.cdk8s.YamlOutputType

fun main() {
  val app = App(AppProps.builder().yamlOutputType(YamlOutputType.FILE_PER_CHART).build())
  val stage = Stage.fromEnv() ?: Stage.DEV
  val env = Environment.fromConfig(stage)
  ShowcaseChart(app, env)
  app.synth()
}
