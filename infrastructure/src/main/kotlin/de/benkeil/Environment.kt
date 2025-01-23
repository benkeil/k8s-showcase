package de.benkeil

import de.benkeil.model.DefaultEnvironment
import de.benkeil.model.Stage

data class Environment(
    override val service: String = "showcase",
    override val awsAccountId: String,
    override val awsRegion: String,
    override val stage: Stage,
    val version: String
) : DefaultEnvironment {
  companion object {
    fun fromConfig(stage: Stage): Environment =
        Environment(
            awsAccountId = "668628784939",
            awsRegion = "eu-central-1",
            stage = stage,
            version =
                when (stage) {
                  Stage.LIVE -> "1.0.0"
                  Stage.DEV -> "1.0.0"
                  Stage.TEST -> "1.0.0"
                },
        )
  }
}
