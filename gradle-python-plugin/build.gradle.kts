plugins {
    id("java-gradle-plugin")
    id("maven-publish")
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    id("com.gradle.plugin-publish") version "0.21.0"
    id("com.google.cloud.artifactregistry.gradle-plugin") version "2.1.4"
}

repositories{
    mavenCentral()
}

publishing {
    repositories {
        mavenLocal()
    }
}

pluginBundle {
    website = "https://github.com/unacast/gradle-python-plugins"
    vcsUrl = "https://github.com/unacast/gradle-python-plugins"
    tags = listOf("python")
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

gradlePlugin {
    plugins {
        create("python-gradle-plugin") {
            id = "com.unacast.plugin.python"
            displayName = "Unacast GCP Composer plugin"
            implementationClass = "com.unacast.gradle.python.PythonPlugin"
            description = "Python packaging using pip and Gradle dependencies"
        }
    }
}