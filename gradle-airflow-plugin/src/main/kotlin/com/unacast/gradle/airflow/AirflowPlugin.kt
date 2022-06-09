package com.unacast.gradle.airflow

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.unacast.gradle.python.PythonPluginExtension
import com.unacast.gradle.python.util.createPythonPath
import com.unacast.gradle.python.util.copyAllFeaturesToTempFolder
import com.unacast.gradle.airflow.tasks.BehaveTask
import com.unacast.gradle.airflow.tasks.AirflowRunTask
import com.unacast.gradle.python.PythonPlugin
import org.gradle.api.tasks.Exec

@Suppress("UNUSED_VARIABLE")
class AirflowPlugin : Plugin<Project> {
    val AIRFLOW_GROUP = "Airflow"

    override fun apply(project: Project): Unit= project.run {
        plugins.apply(PythonPlugin::class.java)
        val airflowDagsFolder = file("${property("com.unacast.turbine.airflow.dags.folder")}")
        val extension = extensions.create("airflow", AirflowExtension::class.java, project)
        val pythonExtension = extensions.getByName("python") as PythonPluginExtension

        val behaveTask = tasks.replace("behave", BehaveTask::class.java)
        behaveTask.dagsFolder.set(extension.dagsFolder)
        behaveTask.pythonBin.set(pythonExtension.pythonBin)
        behaveTask.srcFolder.set(pythonExtension.srcFolder)
        behaveTask.runBehave.set(extension.runBehave)

        tasks.register("airflowRun", AirflowRunTask::class.java){
            it.finalizedBy("airflowLog")
            it.dagsFolder.set(extension.dagsFolder)
            it.pythonBin.set(pythonExtension.pythonBin)
            it.srcFolder.set(pythonExtension.srcFolder)
            it.runBehave.set(extension.runBehave)
        }

        /*project.tasks.register("validate") {
            group = AIRFLOW_GROUP
            description = "Validate that the DAGs are able to be parsed"
            doLast {
                val pythonExtension = project.extensions.getByName("python") as PythonPluginExtension
                val srcFolder = createPythonPath(project)
                val dagsFolder = project.file(extension.dagsFolder.get()).absolutePath
                val pythonPath = project.file(pythonExtension.pythonBin.get())
                val featureFolder = copyAllFeaturesToTempFolder(project)

                project.exec {
                    workingDir(featureFolder)
                    environment("AIRFLOW__WEBSERVER__WEB_SERVER_NAME", "localhost")
                    environment("AIRFLOW__CORE__DAGS_FOLDER", dagsFolder)
                    environment("PYTHONPATH", "${srcFolder}")
                    environment("PATH", "${pythonPath.parent}")
                    commandLine(
                        pythonPath.absolutePath,
                        "-m",
                        "behave",
                        "--tags",
                        "validate",
                        "--no-junit",
                        "--no-skipped",
                        "--summary",
                        "--format",
                        "pretty",
                        "--color"
                    )
                }
            }
        }*/

        tasks.register("airflowLog") {
            it.group = "Airflow"
            it.description = "Tail the airflow webserver log"
            it.doLast {
                exec {spec ->
                    //TODO: Bad. We hardcode that we know the location of the file, generated in airflow.sh
                    spec.commandLine("tail", "-f", "${rootDir}/.airflow/${name}/airflow-webserver.out")
                }
            }
        }

        tasks.register("airflowStop"){
            it.group = "Airflow"
            it.description = "Stop local Airflow"
            it.doLast{
                // Very hackish
                val webserverPidFile = file("${rootDir}/.airflow/${name}/airflow-webserver.pid")
                val schedulerPidFile = file("${rootDir}/.airflow/${name}/airflow-scheduler.pid")
                try {
                    if (webserverPidFile.exists()){
                        exec{spec ->
                            spec.commandLine("kill", "${webserverPidFile.readText().trim()}")
                        }
                    }

                }finally {
                    if (webserverPidFile.exists()){
                        exec{spec ->
                            spec.commandLine("kill", "${schedulerPidFile.readText().trim()}")
                        }
                    }
                }
            }

        }

        tasks.register("airflowKillAll"){
            it.group = "Airflow"
            it.description = "Kill all local running Airflow"
            it.doLast{
                // Very hackish. Again.
                exec{spec ->
                    spec.commandLine("bash", "-c", "ps aux | grep airflow | grep -v \"grep airflow\" | awk '{print \$2}' | xargs kill")
                }
            }
        }

    }
}
