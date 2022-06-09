package com.unacast.gradle.python.util

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

fun useProjectDependenciesAsPythonPath(project: Project): Boolean =
    "true".equals(project.property("com.unacast.turbine.python.use_project_dependencies_in_pythonpath") as String)

fun createPythonPath(project: Project): String {
    if (useProjectDependenciesAsPythonPath(project)) {
        return "${project.projectDir}/src:" + project.configurations
            .map { it.dependencies.toList() }
            .filter { it.isNotEmpty() }
            .flatten()
            .filter { it is ProjectDependency }
            .map {
                "${(it as ProjectDependency).dependencyProject.projectDir}/"
            }
            .joinToString { ":" }
    } else {
        return "${project.projectDir}/src"
    }

}

fun copyAllFeaturesToTempFolder(project: Project): String {
    //Copy all features from turbine_behave into a temp folder
    val destination = "${project.property("com.unacast.turbine.tmp")}/features_${project.name}"
    project.file(destination).deleteRecursively()
    project.project(":turbine_behave")
        .projectDir
        .copyRecursively(project.file(destination))
    //Copy all features from project into temp folder
    val projectFeatures = project.file("${project.projectDir}/features")
    if (projectFeatures.exists()) projectFeatures.copyRecursively(project.file("${destination}/features/"))

    return destination
}

