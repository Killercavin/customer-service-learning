val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.2.0"
}

group = "app"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-config-yaml")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.13")

    // Exposed + Flyway + HikariCP + PostgresSQL
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.postgresql:postgresql:42.7.3")

    // HikariCP connection pool
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Flyway for migrations
    implementation("org.flywaydb:flyway-core:9.22.3")

    // Java Time
    implementation("org.jetbrains.exposed:exposed-java-time:0.43.0")

    // Ktor Content Negotiation plugin
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.9")

    // Kotlinx serialization JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Ktor plugin for Kotlinx Serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.9")

}
