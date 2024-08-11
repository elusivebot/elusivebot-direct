plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.10"
    id("com.diffplug.spotless") version "6.25.0"
    application
}

spotless {
    kotlin {
        diktat()
        toggleOffOn()
    }
    kotlinGradle {
        diktat()
    }
}

repositories {
    mavenCentral()
}

run {
    if (project.hasProperty("internalMavenUrl")) {
        val internalMavenUsername: String by project
        val internalMavenPassword: String by project
        val internalMavenUrl: String by project

        repositories {
            maven {
                credentials {
                    username = internalMavenUsername
                    password = internalMavenPassword
                }
                url = uri("$internalMavenUrl/releases")
                name = "Internal-Maven-Releases"
            }
        }

        repositories {
            maven {
                credentials {
                    username = internalMavenUsername
                    password = internalMavenPassword
                }
                url = uri("$internalMavenUrl/snapshots")
                name = "Internal-Maven-Snapshots"
            }
        }
    } else {
        repositories {
            mavenLocal()
        }
    }
}

group = "com.sirnuke.elusivebot"

dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("com.uchuhimo:konf:1.1.2")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.8.0.202311291450-r")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("io.ktor:ktor-network:2.3.12")

    implementation("com.sirnuke.elusivebot:elusivebot-schema:0.1.0-SNAPSHOT")
    implementation("com.sirnuke.elusivebot:elusivebot-common:0.1.0-SNAPSHOT")

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

tasks.register("printVersion") {
    doLast {
        @Suppress("DEBUG_PRINT")
        println(project.version)
    }
}
