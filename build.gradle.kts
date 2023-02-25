import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File
import java.io.FileInputStream
import java.util.Properties
import java.io.InputStreamReader

plugins {
    application
    kotlin("jvm") version "1.7.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.github.leandrochp.registrationhistoryservice"
version = "1.0.0"

repositories {
    mavenCentral()
}

val vertxVersion = "4.3.8"
val mainPkgAndClass = "com.github.leandrochp.registrationhistoryservice.application.Main"

application {
    mainClass.set(mainPkgAndClass)
}

sourceSets {
    create("componentTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

dependencies {
    //vertx
    implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
    implementation("io.vertx:vertx-web")
    implementation("io.vertx:vertx-lang-kotlin")
    implementation("io.vertx:vertx-dependencies:4.3.8")
    //kotlin
    implementation(kotlin("stdlib-jdk8"))
    //koin
    implementation("io.insert-koin:koin-core:3.2.2")
    //kconfig
    implementation("com.natpryce:konfig:1.6.10.0")
    //jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.1")
    // mongo
    implementation("org.mongodb:mongo-java-driver:3.12.8")
    //ulid
    implementation("io.azam.ulidj:ulidj:1.0.1")
    //log
    implementation("ch.qos.logback:logback-classic:1.4.5")
    //validations
    implementation("br.com.caelum.stella:caelum-stella-core:2.1.6")

    testImplementation("io.mockk:mockk:1.12.4")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testImplementation("org.assertj:assertj-core:3.22.0")

}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "11"

tasks.withType<CreateStartScripts> { mainClass.set(mainPkgAndClass) }

tasks.withType<ShadowJar> {
    archiveClassifier.set("fat")
    manifest {
        attributes(mapOf("Main-Class" to mainPkgAndClass))
    }
    mergeServiceFiles()
}

tasks.create("componentTest", Test::class) {
    description = "Runs the component tests"
    group = "verification"

    testClassesDirs = sourceSets["componentTest"].output.classesDirs
    classpath = sourceSets["componentTest"].runtimeClasspath

    useJUnitPlatform()
}

tasks.withType<Test> {
    loadEnv(environment, file("test.env"))
    useJUnitPlatform()
}

tasks.test {
    finalizedBy("componentTest")
}

tasks.withType<JavaExec> {
    loadEnv(environment, file("local.env"))
}

fun loadEnv(environment: MutableMap<String, Any>, file: File) {
    val properties = Properties()
    InputStreamReader(FileInputStream(file), Charsets.UTF_8).use {
        properties.load(it)
    }.also {
        properties.forEach { key, value ->
            environment[key.toString()] = value
        }
    }
}
