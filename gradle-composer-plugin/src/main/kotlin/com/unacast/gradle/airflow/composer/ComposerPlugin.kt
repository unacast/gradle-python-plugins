package com.unacast.gradle.airflow.composer

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.GradleException
import org.gradle.api.tasks.Exec

@Suppress("UNUSED_VARIABLE")
class ComposerPlugin : Plugin<Project> {
    val COMPOSER_GROUP = "Composer"

    override fun apply(project: Project) {
        project.configurations.create("composer"){
            it.isCanBeResolved = false
        }
        val COMPOSER_INFO_JSON = "build/composer.json"

        project.apply(mapOf(Pair("plugin", "com.unacast.plugin.airflow")))
        val extension = project.extensions.create("composer", ComposerExtension::class.java)

        project.tasks.register("assertComposerProperties"){
            it.doLast{
                val env = "${extension.githubEnvironment.get()}"
                if(!env.contains(Regex(".*/.*"))){
                    throw GradleException("Github environment must follow the format '<client>/<environment>', while environment is set to $env")
                }
            }
        }

        project.tasks.register("composerGetInfo", Exec::class.java){
            it.description="Gets information about the composer this module is set to deploy to. Information is stored into $COMPOSER_INFO_JSON"
            it.commandLine("bash", "-c", "gcloud composer environments describe ${extension.composerEnvironment.get()} " +
                    "--location=${extension.location.get()} " +
                    "--project ${extension.gcpProject.get()} " +
                    "--format json " +
                    "> $COMPOSER_INFO_JSON")
        }

        project.tasks.register("composerInfo"){
            it.group = COMPOSER_GROUP
            it.description="Show information about the composer this module is set to deploy to."
        }

        project.tasks.register("listComposerDeploys", Exec::class.java){
            it.group = COMPOSER_GROUP
            it.description = "List last deploys to this composer"
            it.dependsOn("assertComposerProperties")
            it.commandLine("${project.rootDir}/bin/deployments.sh", "${extension.githubEnvironment.get()}")
        }


        project.tasks.register("deployComposer"){
            it.group=COMPOSER_GROUP
            it.description="Deploy code to Composer"
        }

    }
}
