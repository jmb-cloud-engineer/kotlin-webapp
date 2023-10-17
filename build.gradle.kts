plugins {
    kotlin("jvm") version "1.9.0"
    application
    id("org.jlleitschuh.gradle.ktlint") version "11.3.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

ktlint {
    disabledRules.set(setOf("no-wildcard-imports"))
}

dependencies {
    // Server and routing libraries
    implementation("io.ktor:ktor-server-core:2.1.2")
    implementation("io.ktor:ktor-server-netty:2.1.2")
    // Logging libraries
    implementation("ch.qos.logback:logback-classic:1.4.4")
    implementation("org.slf4j:slf4j-api:2.0.3")
    // Ktor Plugins
    implementation("io.ktor:ktor-server-status-pages:2.1.2")
    // Configuration HOBON files dependencies
    implementation("com.typesafe:config:1.4.2")
    // JSON Parsing
    implementation("com.google.code.gson:gson:2.10")
    // DB Dependencies
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("com.h2database:h2:2.1.214")
    // DB Migration dependencies
    implementation("org.flywaydb:flyway-core:9.5.1")
    // Testing Dependencies
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}
