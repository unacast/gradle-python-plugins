rootProject.name = "composer"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
include("gradle-python-plugin")
include("gradle-airflow-plugin")
include("gradle-composer-plugin")