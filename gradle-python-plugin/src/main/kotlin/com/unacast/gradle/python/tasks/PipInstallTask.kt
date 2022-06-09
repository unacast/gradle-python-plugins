package com.unacast.gradle.python.tasks

import com.unacast.gradle.python.util.useProjectDependenciesAsPythonPath
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class PipInstallTask() : AbstractPythonTask() {
    @get:Input
    abstract val pipPath: Property<String>

    @get:Input
    abstract val constraint: Property<String>

    init {
        super.setDescription("This installs all dependencies in the venv.")
    }

    @TaskAction
    fun install() {
        val filterDevelopmentDependency = useProjectDependenciesAsPythonPath(project)
        println("filter $filterDevelopmentDependency")
        project.configurations
            .map { it.dependencies.toList() }
            .filter { it.isNotEmpty() }
            .flatten()
            .filter { if (filterDevelopmentDependency) it !is ProjectDependency else true }
            .map {
                when (it) {
                    is ProjectDependency -> {
                        Pair(false, listOf("-e", "${it.dependencyProject.projectDir}/"))
                    }
                    is ModuleDependency -> {
                        Pair(it.isTransitive, listOf("${it.name}${it.version ?: ""}"))
                    }
                    is FileCollectionDependency -> {
                        Pair(false, listOf("-r", it.files.singleFile.path))
                    }
                    else -> {
                        throw GradleException("Unknown dependency type $it")
                    }
                }
            }.groupBy { it.first }
            .forEach {
                var cmdArgs = mutableListOf<String>(
                    pipPath.get(),
                    "install",
                    "--prefer-binary"
                ) + it.value.map { install -> install.second }.flatten()

                if (!it.key) { //is not transitive, use --no-deps
                    cmdArgs += listOf("--no-deps")
                }
                if (constraint.get() != "") {
                    cmdArgs += listOf("--constraint", constraint.get())
                }
                project.exec { spec ->
                    spec.commandLine(cmdArgs)
                }
            }

    }
}