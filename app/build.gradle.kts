plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    id("com.diffplug.spotless") version "6.20.0"
    application
}

spotless {
    kotlin {
        diktat()
    }
    kotlinGradle {
        diktat()
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

group = "com.degrendel.elusivebot"

dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.7")
    implementation("com.uchuhimo:konf:1.1.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("io.ktor:ktor-network:2.3.2")
    implementation("org.apache.kafka:kafka-clients:3.6.0")

    implementation("com.degrendel.elusivebot:elusivebot-schema:0.1.0-SNAPSHOT")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("com.sirnuke.elusivebot.direct.AppKt")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
