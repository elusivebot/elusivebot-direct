plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    application
}

repositories {
    mavenCentral()
}

dependencies {
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
