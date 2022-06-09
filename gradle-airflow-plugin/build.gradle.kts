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
    implementation(project(":gradle-python-plugin"))
}

gradlePlugin {
    plugins {
        create("airflow-gradle-plugin") {
            id = "com.unacast.plugin.airflow"
            displayName = "Unacast Airflow plugin"
            implementationClass = "com.unacast.gradle.airflow.AirflowPlugin"
            description = "Running Apache Airflow locally using Gradle"
        }
    }
}