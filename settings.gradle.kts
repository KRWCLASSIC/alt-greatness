pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.minecraftforge.net")
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net")
        maven("https://maven.neoforged.net/releases")
        maven("https://api.modrinth.com/maven")
        maven("https://jitpack.io") {
            content {
                includeGroupByRegex("com\\.github\\..*")
            }
        }
    }

    resolutionStrategy.eachPlugin {
        when (requested.id.id) {
            "com.replaymod.preprocess" -> useModule("com.github.replaymod:preprocessor:5bf41e6c35")
            "com.replaymod.preprocess-root" -> useModule("com.github.replaymod:preprocessor:5bf41e6c35")
        }
    }
}

dependencyResolutionManagement {
    versionCatalogs.create("libs")
}

rootProject.buildFileName = "root.gradle.kts"

listOf(
    "1.16.5-fabric",
    "1.16.5-forge",
    "1.17.1-fabric",
    "1.17.1-forge",
    "1.18.2-fabric",
    "1.18.2-forge",
    "1.19.2-fabric",
    "1.19.2-forge",
    "1.19.4-fabric",
    "1.19.4-forge",
    "1.20.1-fabric",
    "1.20.1-forge",
    "1.21.1-fabric",
    "1.21.1-neoforge"
).forEach { version ->
    include(":$version")
    project(":$version").apply {
        projectDir = file("versions/$version")
        buildFileName = "../../build.gradle.kts"
    }
}
