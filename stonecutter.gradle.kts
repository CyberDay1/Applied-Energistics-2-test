import dev.kikugie.stonecutter.build.param.StonecutterBuildProperties
import dev.kikugie.stonecutter.controller.StonecutterControllerExtension
import dev.kikugie.stonecutter.data.StonecutterProject
import org.gradle.api.DefaultTask

plugins {
    id("dev.kikugie.stonecutter")
}

val activeVersion = providers.gradleProperty("sc.version").getOrElse("1.21.4")

stonecutter active activeVersion

abstract class StonecutterChiseledTask : DefaultTask() {
    fun ofTask(name: String) {
        val controller = project.extensions.getByType(StonecutterControllerExtension::class.java)
        controller.tasks.named(name).get().values.forEach { dependsOn(it) }
    }
}

private val StonecutterControllerExtension.chiseled: Class<out StonecutterChiseledTask>
    get() = StonecutterChiseledTask::class.java

tasks.register("ciBuild", stonecutter.chiseled) {
    ofTask("build")
}

@Suppress("UNCHECKED_CAST")
private val versionMatrix: Map<String, Map<String, String>> =
    (project.extensions.extraProperties["stonecutterVersionMatrix"]
            as? Map<String, Map<String, String>>)
        ?: emptyMap()

private val StonecutterProject.data: Map<String, String>
    get() = versionMatrix[version] ?: emptyMap()

private val StonecutterBuildProperties.consts: MutableMap<String, String>
    get() {
        val extra = node.project.extensions.extraProperties
        val key = "stonecutter.constMap"
        if (!extra.has(key)) {
            extra.set(key, mutableMapOf<String, String>())
        }
        @Suppress("UNCHECKED_CAST")
        return extra.get(key) as MutableMap<String, String>
    }

stonecutter.parameters {
    val current = stonecutter.current ?: error("Stonecutter version is not selected")
    consts["MC"] = current.version

    val neoForge = current.data["NEOFORGE"]
        ?: error("Missing NEOFORGE version for ${current.version}")
    consts["NEOFORGE"] = neoForge

    val packFormat = current.data["PACK_FORMAT"]
        ?: error("Missing PACK_FORMAT for ${current.version}")
    consts["PACK_FORMAT"] = packFormat

    node.project.extensions.extraProperties.set("MC", current.version)
    node.project.extensions.extraProperties.set("minecraft_version", current.version)
    node.project.extensions.extraProperties.set("NEOFORGE", neoForge)
    node.project.extensions.extraProperties.set("neoforge_version", neoForge)
    node.project.extensions.extraProperties.set("neoforge_version_range", "[${neoForge},)")
    node.project.extensions.extraProperties.set("PACK_FORMAT", packFormat)
}
