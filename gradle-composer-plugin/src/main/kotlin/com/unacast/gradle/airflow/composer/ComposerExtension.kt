package com.unacast.gradle.airflow.composer

import org.gradle.api.Project
import org.gradle.api.provider.Property

@Suppress("UNUSED_PARAMETER")
abstract class ComposerExtension(project: Project) {
    abstract val githubEnvironment: Property<String>
    abstract val composerEnvironment: Property<String>
    abstract val location: Property<String>
    abstract val gcpProject: Property<String>
}