package de.benkeil.model

enum class Stage {
  LIVE,
  DEV,
  TEST,
  ;

  override fun toString(): String = name

  companion object {
    fun fromEnv(): Stage? {
      val environment = System.getenv()["ENVIRONMENT"]
      return entries.firstOrNull { it.name == environment?.uppercase() }
    }

    fun fromEnvOrDefault(): Stage = Stage.fromEnv() ?: DEV
  }
}
