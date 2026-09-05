import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.1.20"
    application
}

repositories { mavenCentral() }
dependencies { implementation("com.parseapi:parseapi:0.3.0") }
kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
application { mainClass.set("ExampleKt") }
