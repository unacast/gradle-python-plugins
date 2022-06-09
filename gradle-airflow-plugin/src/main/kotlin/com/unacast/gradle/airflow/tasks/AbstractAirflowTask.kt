package com.unacast.gradle.airflow.tasks

import org.gradle.api.DefaultTask

abstract class AbstractAirflowTask : DefaultTask(){
    init {
        super.setGroup("Airflow")
    }
}