package de.benkeil

import com.hashicorp.cdktf.App
import de.benkeil.model.Stage

fun main() {
  val app = App()
  val stage = Stage.fromEnv() ?: Stage.DEV
  val env = Environment.fromConfig(stage)
  ShowcaseStack(app, env)
  app.synth()
}
