package app.config

import org.slf4j.LoggerFactory
import java.io.File
import java.util.Properties

object EnvConfig {
    private val logger = LoggerFactory.getLogger(EnvConfig::class.java)
    private val properties = loadEnvFile()

    private fun loadEnvFile(): Properties {
        val props = Properties()
        val envFile = File(".env")

        return try {
            if (envFile.exists()) {
                envFile.bufferedReader().useLines { lines ->
                    lines.filter { it.isNotBlank() && !it.startsWith("#") }
                        .forEach { line ->
                            val (key, value) = line.split("=", limit = 2)
                            props[key.trim()] = value.trim()
                        }
                }
                logger.info("Loaded environment variables from .env file.")
            } else {
                logger.warn("Environment file not found — using system environment variables.")
            }
            props
        } catch (e: Exception) {
            logger.error("Failed to load .env file: ${e.message}")
            props
        }
    }

    fun getEnv(key: String, defaultValue: String? = null): String {
        return properties.getProperty(key)
            ?: System.getenv(key)
            ?: defaultValue
            ?: throw IllegalStateException("Environment variable '$key' is not set and no default provided.")
    }

    // Database configuration - supports both individual components and full URL
    val databaseHost: String by lazy {
        getEnv("DB_HOST", "localhost")
    }

    val databasePort: String by lazy {
        getEnv("DB_PORT", "5432")
    }

    val databaseName: String by lazy {
        getEnv("DB_NAME", "postgres")
    }

    val databaseUser: String by lazy {
        getEnv("DB_USER")
    }

    val databasePassword: String by lazy {
        getEnv("DB_PASSWORD")
    }

    // Clean JDBC URL without credentials
    val databaseUrl: String by lazy {
        "jdbc:postgresql://$databaseHost:$databasePort/$databaseName?sslmode=require&user=$databaseUser&password=$databasePassword"
    }
}