import org.gradle.api.plugins.JavaPlugin

plugins {
    id("net.neoforged.moddev")
    id("dev.kikugie.stonecutter")
    id("com.diffplug.spotless")
}

val minecraftConfiguration = configurations.maybeCreate("minecraft")

configurations.named(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME) {
    extendsFrom(minecraftConfiguration)
}

configurations.named(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME) {
    extendsFrom(minecraftConfiguration)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.shedaniel.me/") {
        content {
            includeGroup("me.shedaniel")
            includeGroup("me.shedaniel.cloth")
        }
    }
    maven("https://www.cursemaven.com") {
        content {
            includeGroup("curse.maven")
        }
    }
    maven("https://maven.blamejared.com/") {
        content {
            includeGroup("mezz.jei")
        }
    }
    maven("https://libraries.minecraft.net/")
    mavenLocal()
    mavenCentral()
}

neoForge {
    version = property("NEOFORGE") as String
}

dependencies {
    val neoForgeVersion = property("NEOFORGE") as String
    if (neoForgeVersion.contains('x')) {
        logger.lifecycle("[stonecutter] TODO: define NeoForge artifact for version $neoForgeVersion before enabling full builds.")
    } else {
        add("minecraft", "net.neoforged:neoforge:$neoForgeVersion")
    }
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
