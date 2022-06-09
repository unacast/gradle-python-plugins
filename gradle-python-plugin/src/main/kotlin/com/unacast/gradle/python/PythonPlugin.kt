package com.unacast.gradle.python

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Exec
import com.unacast.gradle.python.util.useProjectDependenciesAsPythonPath
import com.unacast.gradle.python.util.createPythonPath
import com.unacast.gradle.python.tasks.PipInstallTask

@Suppress("UNUSED_VARIABLE")
open class PythonPlugin : Plugin<Project> {
    companion object {
        const val PYTHON_GROUP: String = "Python"
    }

    override fun apply(project: Project) : Unit = project.run {
        configurations.create("provided"){
            it.isCanBeResolved = false
        }
        configurations.create("compile"){
            it.isCanBeResolved = false
        }

        val extension = extensions.create("python", PythonPluginExtension::class.java, project)
        tasks.register("pyenvInstall", Exec::class.java){
            it.group = "Python"
            it.description = "Install required version of Python in Pyenv."
            it.commandLine("${project.rootDir}/bin/install_pyenv.sh", "${extension.pythonVersion.get()}")
        }

        tasks.register("venv", Exec::class.java) {
            it.description = "This creates a local venv in the project directory"
            it.group = "Python"
            it.dependsOn("pyenvInstall") // TODO: We should rewrite this to be pyenv agnostic
            it.commandLine("${project.rootDir}/bin/venv.sh", "${extension.pythonVersion.get()}")
        }

        tasks.register("pipInstall", PipInstallTask::class.java) {
            it.dependsOn("checkPythonEnvironment")
            it.mustRunAfter("venv")
            it.constraint.set(extension.constraint)
            it.pipPath.set(extension.pipPath)
        }

        project.tasks.register("clearVenv", Delete::class.java) {
            it.group = PYTHON_GROUP
            it.description = "Clear the venv for this module"
            it.delete = setOf(extension.venvPath.get())
        }

        project.tasks.register("test", Exec::class.java) {
            it.group = "Python"
            it.description = "Run pytest for this module"
            it.dependsOn("checkPythonEnvironment")
            it.onlyIf { project.file("tests").exists() }
            it.environment("PYTHONPATH", createPythonPath(project))
            it.commandLine(
                listOf(
                    extension.pythonBin.get(),
                    "-m",
                    "pytest"
                ) + (project.property("com.unacast.turbine.pytest.args") as String).split(" ")
            )
        }

        // Need to have it still, because the CI thinks every module has behave task.
        project.tasks.register("behave") {
            it.onlyIf {false }
        }


        project.tasks.register("pipdepTree", Exec::class.java) {
            it.group = "Python"
            it.description = "Run python command pipdeptree in this project"
            it.dependsOn("checkPythonEnvironment")
            it.commandLine(listOf(extension.pythonBin.get(), "-m", "pipdeptree"))
        }

        project.tasks.register("pipFreeze", Exec::class.java) {
            it.group = "Python"
            it.description = "Run python command pipdeptree in this project"
            it.dependsOn("checkPythonEnvironment")
            it.commandLine(listOf(extension.pythonBin.get(), "-m", "pip", "freeze"))
        }

        project.tasks.register("flake8", Exec::class.java) {
            it.group = "Python"
            it.description = "Run flake8"
            it.dependsOn("checkPythonEnvironment")
            it.commandLine(extension.pythonBin.get(), "-m", "flake8", "--statistics", "--config", "${project.rootDir}/.flake8", extension.srcFolder.get())
        }

        project.tasks.register("checkPythonEnvironment") {
            it.group = "Python"
            it.description = "Check if the current environment dependencies are met"
            it.doLast {
                print("Checking existance of pythonpath: ${extension.pythonBin.get()}: ")
                if (project.file(extension.pythonBin.get()).exists()) {
                    println("OK")
                }else{
                    throw GradleException("Could not find pythonpath: ${extension.pythonBin.get()}. Maybe you should run gradlew venv pipInstall?")
                }
            }
        }

        project.tasks.register("dependencyList"){
            it.group = "Python"
            it.description = "Create a file with all the dependencies, minus transitive, sorted. Good for CI tooling to cache the venv etc."
            it.doLast{
                // Create new output file
                val outputFile = project.file(project.property("com.unacast.turbine.python.output.requirementsfile") as String)
                outputFile.delete()
                // If we'd like to filter out project dependencies (not included in venv in Github Actions)
                val filterDevelopmentDependency = useProjectDependenciesAsPythonPath(project)
                project.configurations
                    .map { it.dependencies.toList() }
                    .filter { it.isNotEmpty() }
                    .flatten()
                    .filter{if(filterDevelopmentDependency) it !is ProjectDependency else true}
                    .map{
                        when (it) {
                            is ProjectDependency -> {
                                "${it.dependencyProject.projectDir}"
                            }
                            is ModuleDependency -> {
                                "${it.name}${it.version?:""}"
                            }
                            is FileCollectionDependency -> {
                                it.files.singleFile.path
                            }
                            else -> {
                                throw GradleException("Unknown dependency type $it")
                            }
                        }
                    }
                    .sorted()
                    .forEach { outputFile.appendText("${it}\n") }

                println("Created an output of the dependencies in ${outputFile}")
            }

        }
    }


}
