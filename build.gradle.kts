plugins {
    java
    application
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    id("org.javamodularity.moduleplugin") version "1.8.12"
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "3.1.1"
}

group = "me.qblmchmmd"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitVersion = "5.10.2"


tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("me.qblmchmmd.roming")
    mainClass.set("me.qblmchmmd.roming.HelloApplication")
}
kotlin {
    jvmToolchain(24)
}

javafx {
    version = "24.0.2"
    modules = listOf("javafx.controls", "javafx.fxml")
}

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

dependencies {
    val ktor_version = "3.2.2"
    val logback_version = "1.5.18"
    implementation("io.ktor:ktor-client-core:${ktor_version}")
    implementation("io.ktor:ktor-client-java:${ktor_version}")
    implementation("io.ktor:ktor-client-logging:${ktor_version}")
    implementation("ch.qos.logback:logback-classic:${logback_version}")
    implementation("io.ktor:ktor-client-content-negotiation:${ktor_version}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${ktor_version}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.10.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    implementation("org.jetbrains:annotations:24.1.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
tasks {
    processResources {
        from(sourceSets.main.get().resources.srcDirs)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

jlink {
    forceMerge("org.jetbrains.annotations")
    addExtraDependencies("annotations", "kotlinx", "kotlin", "slf4j")
    imageZip.set(layout.buildDirectory.file("/distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    mergedModule {
        additive = true
        uses("io.ktor.serialization.kotlinx.KotlinxSerializationExtensionProvider")
    }
    jpackage {
        appVersion = "1.0.0"
    }
    launcher {
        name = "Roming"
    }
}
