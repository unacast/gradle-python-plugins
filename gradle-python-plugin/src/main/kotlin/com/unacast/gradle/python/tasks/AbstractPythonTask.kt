package com.unacast.gradle.python.tasks

import com.unacast.gradle.python.PythonPlugin
import org.gradle.api.DefaultTask

abstract class AbstractPythonTask : DefaultTask(){
    init {
        super.setGroup(PythonPlugin.PYTHON_GROUP)
    }
}