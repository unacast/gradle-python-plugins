package com.unacast.gradle.airflow.tasks

import com.unacast.gradle.python.util.copyAllFeaturesToTempFolder
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.api.provider.Property
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input

abstract class BehaveTask : AbstractAirflowTask() {
    @get:Input
    abstract val dagsFolder: Property<String>

    @get:Input
    abstract val pythonBin: Property<String>

    @get:Input
    abstract val srcFolder: Property<String>

    @get:Input
    abstract val runBehave: Property<Boolean>

    init {
        super.setDescription("Run behave features for module")
    }

    override fun onlyIf(spec: Spec<in Task>) {
        super.onlyIf {
            if (runBehave.get() || project.file("${project.projectDir}/features").exists()) {
                true
            } else {
                logger.info("Skipped behave as neither runBehave flag is true or a features folder was found")
                false
            }
        }
    }

    @TaskAction
    fun behave() {
        val featureFolder = copyAllFeaturesToTempFolder(project)
        val pythonBinFile = project.file(pythonBin.get())
        val dagsFolderPath = project.file(dagsFolder.get()).absolutePath
        project.exec {
            it.workingDir(featureFolder)
            it.environment("AIRFLOW__WEBSERVER__WEB_SERVER_NAME", "localhost")
            it.environment("AIRFLOW__CORE__DAGS_FOLDER", dagsFolderPath)
            it.environment("PYTHONPATH", srcFolder)
            it.environment("PATH", "${pythonBinFile.parent}")
            it.commandLine(
                listOf(
                    pythonBinFile.absolutePath,
                    "-m",
                    "behave"
                ) + (project.property("com.unacast.turbine.behave.args") as String).split(" ")
            )
        }
    }
}