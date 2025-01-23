import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  application
}

dependencies { implementation(libs.bundles.infrastructure.implementation) }

kotlin { jvmToolchain(17) }

application { mainClass.set("de.benkeil.ApplicationKt") }

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions { freeCompilerArgs.add("-Xcontext-receivers") }
}
