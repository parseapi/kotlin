import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	kotlin("jvm") version "2.1.20"
	kotlin("plugin.serialization") version "2.1.20"
}

group = "com.parseapi"
version = "0.2.0"

repositories {
	mavenCentral()
}

dependencies {
	api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
	testImplementation(kotlin("test"))
}

// JVM 11 bytecode so Android projects consume it without desugaring surprises.
kotlin {
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_11)
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}

tasks.test {
	useJUnitPlatform()
}

// Live smoke against the edge: gradle smoke (PARSEAPI_KEY in the env).
sourceSets {
	create("smoke") {
		kotlin.srcDir("smoke")
		compileClasspath += sourceSets.main.get().output + configurations.runtimeClasspath.get()
		runtimeClasspath += output + compileClasspath
	}
}

tasks.register<JavaExec>("smoke") {
	group = "verification"
	description = "Live smoke against the edge (PARSEAPI_KEY required)"
	classpath = sourceSets["smoke"].runtimeClasspath
	mainClass.set("SmokeKt")
}
