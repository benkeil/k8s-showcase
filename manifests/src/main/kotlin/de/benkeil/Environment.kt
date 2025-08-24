package de.benkeil

import de.benkeil.model.DefaultEnvironment
import java.io.File
import java.util.Properties

data class Environment(
    override val service: String = "crds",
    val letsencryptUseStaging: Boolean = false,
    val domain: String = "home.keil.pub",
    val email: String = "benkeil.webmaster@pm.me",
    val awsAccessKeyId: String,
    val awsSecretAccessKey: String,
    val awsRegion: String = "eu-central-1",
    val awsHostedZoneId: String = "Z03299852HYZRKYD8XK9W",
    val dockerUsername: String,
    val dockerPassword: String,
) : DefaultEnvironment {
  companion object {
    fun fromConfig(): Environment {
      val properties = readSecret()
      return Environment(
          awsAccessKeyId = properties.getProperty("AWS_ACCESS_KEY_ID"),
          awsSecretAccessKey = properties.getProperty("AWS_SECRET_ACCESS_KEY"),
          dockerUsername = properties.getProperty("DOCKER_USERNAME"),
          dockerPassword = properties.getProperty("DOCKER_PASSWORD"),
      )
    }
  }
}

fun decryptAgeFile(encryptedFile: File, keyFile: File): String =
    ProcessBuilder(
            "age", "--decrypt", "--identity", keyFile.absolutePath, encryptedFile.absolutePath)
        .redirectErrorStream(true)
        .start()
        .let {
          val exitCode = it.waitFor()
          val output = it.inputStream.bufferedReader().readText()
          if (exitCode != 0) {
            throw RuntimeException("age decryption failed:\n$output")
          }
          output
        }

fun readSecret(
    encryptedFile: File = readResourceAsFile("/secret.properties.enc"),
    keyFile: File = File("/Users/ben/.config/sops/age/key.txt")
): Properties =
    decryptAgeFile(encryptedFile, keyFile).let { content ->
      Properties().apply { load(content.byteInputStream()) }
    }
