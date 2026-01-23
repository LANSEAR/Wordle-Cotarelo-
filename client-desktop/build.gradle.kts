import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.Copy
import org.gradle.api.file.DuplicatesStrategy


plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(compose.materialIconsExtended) // 👈 ESTA ES LA CLAVE
}

kotlin {
    jvmToolchain(21)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

/**
 * ✅ Asegura que src/main/resources entra en el classpath
 * y se copie a build/resources/main
 */
sourceSets {
    main {
        resources.srcDirs("src/main/resources")
    }
}


tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}
tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

compose.desktop {
    application {
        mainClass = "com.cotarelo.wordle.client.MainKt"
    }
}
