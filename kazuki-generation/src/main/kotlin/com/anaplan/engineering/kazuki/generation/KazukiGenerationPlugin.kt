package com.anaplan.engineering.kazuki.generation

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPluginExtension
import java.io.File

open class KazukiGenerationPluginExtension @javax.inject.Inject constructor(objectFactory: ObjectFactory) {
    var generatedSrc: String = "generatedSrc"
}


internal const val PackageName = "com.anaplan.engineering.kazuki.core"
internal const val InternalPackageName = "com.anaplan.engineering.kazuki.core.internal"

class KazukiGenerationPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // TODO -- get this working!?
        project.afterEvaluate {
            it.extensions.getByType(JavaPluginExtension::class.java).sourceSets.getByName("main").java.srcDirs.add(File(project.buildDir, "generated/kazuki/main/kotlin"))
        }
        project.extensions.create("kazuki", KazukiGenerationPluginExtension::class.java, project.objects)
        project.createKazukiTask("generateFunctions", FunctionGeneratorTask::class.java)
        project.createKazukiTask("generateTuples", TupleGeneratorTask::class.java)
    }
}

internal const val kazukiTaskGroup = "kazuki"

internal fun Project.createKazukiTask(name: String, type: Class<out Task>) =
    tasks.create(
        mapOf<String, Any>(
            "name" to name,
            "type" to type,
            "group" to kazukiTaskGroup
        )
    )

internal fun Project.generationSrcDir() = File(buildDir, "generated/kazuki/main/kotlin")
