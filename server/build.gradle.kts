import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    application
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}
dependencies {
    implementation(project(":shared"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.cotarelo.wordle.server.MainKt")
}

kotlin {
    jvmToolchain(21)
}
