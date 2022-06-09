package com.unacast.gradle.python

import org.gradle.api.Project
import org.gradle.api.provider.Property

open class PythonPluginExtension(project: Project) {
    val venvPath: Property<String> = project.objects.property(String::class.java)
        .convention(project.provider({ project.property("com.unacast.turbine.venv.path") as String }))

    val pipPath: Property<String> = project.objects.property(String::class.java)
        .convention(project.provider({ project.property("com.unacast.turbine.pip.path") as String }))

    val pythonBin: Property<String> = project.objects.property(String::class.java)
        .convention(project.provider({ project.property("com.unacast.turbine.python.path") as String }))

    val pythonVersion: Property<String> = project.objects.property(String::class.java)
        .convention(project.provider({ project.property("com.unacast.turbine.python.version") as String }))

    val srcFolder: Property<String> = project.objects.property(String::class.java)
        .convention(project.provider({ project.property("com.unacast.turbine.src.folder") as String }))

    val constraint: Property<String> = project.objects.property(String::class.java)
        .convention(project.provider({ "" }))

}