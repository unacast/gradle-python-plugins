package com.unacast.gradle.airflow.composer.tasks

import com.unacast.gradle.airflow.tasks.AbstractAirflowTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

abstract class DeployComposerTask : AbstractAirflowTask() {
    @get:Input
    abstract val dagsFolder: Property<String>

    @get:Input
    abstract val pythonBin: Property<String>

    @get:Input
    abstract val srcFolder: Property<String>

    @get:Input
    abstract val runBehave: Property<Boolean>

    init {
        super.setDescription("Start local Airflow")
    }

    @TaskAction
    fun run() {
        val pythonBinFile = project.file(pythonBin.get())
        project.exec{
            it.environment("AIRFLOW__CORE__LOAD_EXAMPLES", "false")
            it.environment("AIRFLOW__CORE__DAGS_FOLDER", project.file(dagsFolder).absolutePath)
            it.environment("AIRFLOW__WEBSERVER__WEB_SERVER_NAME", "localhost")
            it.environment("AIRFLOW__WEBSERVER__EXPOSE_CONFIG", "true")
            it.environment("DATA_FOLDER", "${project.rootDir}/.airflow/data")
            // TODO: Make name more unique
            it.environment("AIRFLOW_HOME", "${project.rootDir}/.airflow/${project.name}")
            it.environment("OBJC_DISABLE_INITIALIZE_FORK_SAFETY", "YES")
            it.environment("PYTHONPATH", project.file(srcFolder.get()).absolutePath)
            it.environment("PATH", "${pythonBinFile.parent}:/usr/bin/:/bin")
            it.commandLine("${project.rootDir}/bin/airflow.sh", pythonBinFile.absolutePath)
        }
    }
}