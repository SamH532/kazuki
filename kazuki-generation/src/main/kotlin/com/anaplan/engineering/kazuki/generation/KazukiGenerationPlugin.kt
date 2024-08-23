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


internal const val RootPackageName = "com.anaplan.engineering.kazuki.core"
internal const val InternalPackageName = "$RootPackageName.internal"

class KazukiGenerationPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // TODO -- get this working!?
        project.afterEvaluate {
            val sourceSets = it.extensions.getByType(JavaPluginExtension::class.java).sourceSets
            sourceSets.getByName("main").java.srcDirs.add(File(project.buildDir, "generated/kazuki/main/kotlin"))
            sourceSets.getByName("test").java.srcDirs.add(File(project.buildDir, "generated/kazuki/test/kotlin"))
        }
        project.extensions.create("kazuki", KazukiGenerationPluginExtension::class.java, project.objects)
        project.createKazukiTask("generateFunctions", FunctionGeneratorTask::class.java)
        project.createKazukiTask("generateTuples", TupleGeneratorTask::class.java)
        project.createKazukiTask("generateRecordTests", RecordTestGeneratorTask::class.java)
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
internal fun Project.generationTestSrcDir() = File(buildDir, "generated/kazuki/test/kotlin")
