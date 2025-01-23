plugins {
  kotlin("jvm")
  application
  alias(libs.plugins.ktor)
}

dependencies { implementation(libs.bundles.application.implementation) }

kotlin { jvmToolchain(17) }

application { mainClass.set("de.benkeil.ApplicationKt") }
