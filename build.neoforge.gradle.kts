plugins {
    id("net.neoforged.moddev")
    id("net.neoforged.moddev.repositories")
    id("dev.kikugie.stonecutter")
    id("com.diffplug.spotless")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val neoForgeVersionProvider = providers.gradleProperty("NEOFORGE").orElse(
    providers.provider { project.findProperty("NEOFORGE")?.toString().orEmpty() }
)
val neoForgeVersion = neoForgeVersionProvider.orNull?.takeUnless { it.isEmpty() }
    ?: error("Missing NEOFORGE version")
val modId = project.findProperty("modid")?.toString() ?: error("Missing modid property")
val accessTransformerFile = rootProject.file("src/main/resources/META-INF/accesstransformer.cfg")
val hasAccessTransformers = accessTransformerFile.exists() &&
    accessTransformerFile.readLines().any { line ->
        val trimmed = line.trim()
        trimmed.isNotEmpty() && !trimmed.startsWith("#") && !trimmed.startsWith("//")
    }

neoForge {
    version = neoForgeVersion
    runs {
        register("client") {
            client()
            gameDirectory.set(project.file("run/client"))
        }
        register("server") {
            server()
            gameDirectory.set(project.file("run/server"))
        }
        register("data") {
            data()
            gameDirectory.set(project.file("run/data"))
            programArguments.addAll(listOf("--mod", modId, "--all"))
        }
    }
    if (hasAccessTransformers) {
        accessTransformers.from(accessTransformerFile)
    }
}

dependencies {
    implementation("net.neoforged:neoforge:$neoForgeVersion")
}

spotless {
    java {
        target("src/*/java/**/*.java")
        removeUnusedImports()
        googleJavaFormat("1.17.0").reflowLongStrings()
    }

    json {
        target("src/**/*.json", "src/**/*.mcmeta")
        gson()
    }

    format("misc") {
        target("*.md", "src/**/*.mcfunction", "src/**/*.lang")
        targetExclude("build/**", "out/**")
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        target("*.gradle.kts", "stonecutter.gradle.kts")
        ktlint()
    }

    groovyGradle {
        target("*.gradle")
        greclipse()
    }
}

tasks.withType<Test> {
    onlyIf { project.hasProperty("enableTests") }
}
