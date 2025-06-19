package app.config

import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory

object FlywayConfig {
    private val logger = LoggerFactory.getLogger(FlywayConfig::class.java)

    fun migrate() {
        try {
            val flyway = Flyway.configure()
                .dataSource(
                    EnvConfig.databaseUrl,
                    EnvConfig.databaseUser,
                    EnvConfig.databasePassword
                )
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)           // Start baseline if schema exists
                .validateOnMigrate(false)          // Prevent crash on validation errors (DEV only)
                .cleanDisabled(false)              // Enable Flyway clean() if needed
                .ignoreMigrationPatterns("*:missing") // Ignore removed migration warnings
                .load()

            val result = flyway.migrate()
            logger.info("Flyway migration complete: ${result.migrationsExecuted} migrations applied.")
        } catch (e: Exception) {
            logger.error("Flyway migration failed", e)
            throw e
        }
    }
}
