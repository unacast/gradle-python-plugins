package com.unacast.gradle.airflow

import com.unacast.gradle.python.PythonPluginExtension
import org.gradle.api.Project
import org.gradle.api.provider.Property

open class AirflowExtension(project: Project) {
    /* We default to not running behave.
    Some project switch it on automatically by having a features folder, but you can
    also force the default features to be run by setting this to true.
     */
    val runBehave: Property<Boolean> = project.objects.property(Boolean::class.java)
        .convention(false)

    val dagsFolder: Property<String> = project.objects.property(String::class.java)
        .convention(project.provider({ project.property("com.unacast.turbine.airflow.dags.folder") as String }))
}