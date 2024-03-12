plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    id("com.diffplug.spotless") version "6.25.0"
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

group = "com.sirnuke.elusivebot"

val kafkaApiVersion = "3.6.1"

dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.12")
    implementation("com.uchuhimo:konf:1.1.2")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.8.0.202311291450-r")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("io.ktor:ktor-network:2.3.2")
    implementation("org.apache.kafka:kafka-streams:$kafkaApiVersion")
    implementation("org.apache.kafka:kafka-clients:$kafkaApiVersion")

    implementation("com.sirnuke.elusivebot:elusivebot-schema:0.1.0-SNAPSHOT")
    implementation("com.sirnuke.elusivebot:elusivebot-common:0.1.0-SNAPSHOT")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    testImplementation("org.apache.kafka:kafka-streams-test-utils:$kafkaApiVersion")
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
