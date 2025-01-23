package de.benkeil.model

interface DefaultEnvironment {
  val service: String
  val stage: Stage
  val awsRegion: String
  val awsAccountId: String
}
