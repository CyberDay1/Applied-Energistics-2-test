import dev.kikugie.stonecutter.build.param.StonecutterBuildProperties
import dev.kikugie.stonecutter.controller.StonecutterControllerExtension
import dev.kikugie.stonecutter.data.StonecutterProject
import org.gradle.api.DefaultTask

plugins {
    id("dev.kikugie.stonecutter")
}

val defaultStonecutterVersion = "1.21.4-neoforge"
val activeVersion = providers.gradleProperty("sc.version").getOrElse(defaultStonecutterVersion)

private data class NeoForgeTarget(
    val mcVersion: String,
    val neoForgeVersion: String,
    val packFormat: String,
)

private class VersionRegistration {
    val extras: MutableMap<String, String> = mutableMapOf()
    fun extra(key: String, value: String) {
        extras[key] = value
    }
}

private val neoForgeTargets = mutableMapOf<String, NeoForgeTarget>()
private val registeredExtras = mutableMapOf<String, Map<String, String>>()

private fun register(version: String, block: VersionRegistration.() -> Unit) {
    val registration = VersionRegistration().apply(block)
    registeredExtras[version] = registration.extras.toMap()

    val mcVersion = registration.extras["MC_VERSION"] ?: registration.extras["MC"]
    val neoForgeVersion = registration.extras["NEOFORGE_VERSION"] ?: registration.extras["NEOFORGE"]
    val packFormat = registration.extras["PACK_FORMAT"]

    if (mcVersion != null && neoForgeVersion != null && packFormat != null) {
        neoForgeTargets[version] = NeoForgeTarget(mcVersion, neoForgeVersion, packFormat)
    }
}

register("1.21.1-neoforge") {
    extra("MC_VERSION", "1.21.1")
    extra("NEOFORGE_VERSION", "21.1.209")
    extra("PACK_FORMAT", "48")
}

register("1.21.2-neoforge") {
    extra("MC_VERSION", "1.21.2")
    extra("NEOFORGE_VERSION", "21.2.1-beta")
    extra("PACK_FORMAT", "57")
}

register("1.21.3-neoforge") {
    extra("MC_VERSION", "1.21.3")
    extra("NEOFORGE_VERSION", "21.3.93")
    extra("PACK_FORMAT", "57")
}

register("1.21.4-neoforge") {
    extra("MC_VERSION", "1.21.4")
    extra("NEOFORGE_VERSION", "21.4.154")
    extra("PACK_FORMAT", "61")
}

register("1.21.5-neoforge") {
    extra("MC_VERSION", "1.21.5")
    extra("NEOFORGE_VERSION", "21.5.95")
    extra("PACK_FORMAT", "71")
}

register("1.21.6-neoforge") {
    extra("MC_VERSION", "1.21.6")
    extra("NEOFORGE_VERSION", "21.6.20-beta")
    extra("PACK_FORMAT", "80")
}

register("1.21.7-neoforge") {
    extra("MC_VERSION", "1.21.7")
    extra("NEOFORGE_VERSION", "21.7.25-beta")
    extra("PACK_FORMAT", "81")
}

register("1.21.8-neoforge") {
    extra("MC_VERSION", "1.21.8")
    extra("NEOFORGE_VERSION", "21.8.47")
    extra("PACK_FORMAT", "81")
}

register("1.21.9-neoforge") {
    extra("MC_VERSION", "1.21.9")
    extra("NEOFORGE_VERSION", "21.9.16-beta")
    extra("PACK_FORMAT", "88")
}

register("1.21.10-neoforge") {
    extra("MC_VERSION", "1.21.10")
    extra("NEOFORGE_VERSION", "21.10.5-beta")
    extra("PACK_FORMAT", "88")
}

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
    val mcVersion = current.data["MC"]
        ?: current.data["MC_VERSION"]
        ?: registeredExtras[current.version]?.get("MC_VERSION")
        ?: neoForgeTargets[current.version]?.mcVersion
        ?: current.version.substringBefore('-')
    consts["MC"] = mcVersion

    val neoForge = current.data["NEOFORGE"]
        ?: current.data["NEOFORGE_VERSION"]
        ?: registeredExtras[current.version]?.get("NEOFORGE_VERSION")
        ?: neoForgeTargets[current.version]?.neoForgeVersion
        ?: error("Missing NEOFORGE version for ${current.version}")
    consts["NEOFORGE"] = neoForge

    val packFormat = current.data["PACK_FORMAT"]
        ?: registeredExtras[current.version]?.get("PACK_FORMAT")
        ?: neoForgeTargets[current.version]?.packFormat
        ?: error("Missing PACK_FORMAT for ${current.version}")
    consts["PACK_FORMAT"] = packFormat

    node.project.extensions.extraProperties.set("MC", mcVersion)
    node.project.extensions.extraProperties.set("minecraft_version", mcVersion)
    node.project.extensions.extraProperties.set("NEOFORGE", neoForge)
    node.project.extensions.extraProperties.set("neoforge_version", neoForge)
    node.project.extensions.extraProperties.set("neoforge_version_range", "[${neoForge},)")
    node.project.extensions.extraProperties.set("PACK_FORMAT", packFormat)
}
