plugins {
    java
    alias(libs.plugins.architecturyPlugin)
    alias(libs.plugins.blossom)
    alias(libs.plugins.shadow)
    id(libs.plugins.loom.get().pluginId)
    id(libs.plugins.preprocessor.get().pluginId)
    id("com.modrinth.minotaur")
}

val modPlatform = Platform.of(project)
buildscript {
    project.extra.set("loom.platform", project.name.substringAfter("-"))
}

java {
    val javaVersion = if (modPlatform.mcVersion >= 12005) JavaVersion.VERSION_21 else if (modPlatform.mcVersion >= 11800) JavaVersion.VERSION_17 else if (modPlatform.mcVersion >= 11700) JavaVersion.VERSION_16 else JavaVersion.VERSION_1_8
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

architectury {
    platformSetupLoomIde()
    when (modPlatform.loader) {
        Platform.Loader.Fabric -> fabric()
        Platform.Loader.Forge -> forge()
        Platform.Loader.NeoForge -> neoForge()
    }
}

if (modPlatform.isFabric) {
    configurations.configureEach {
        resolutionStrategy {
            force("net.fabricmc:fabric-loader:0.16.5")
        }
        if (modPlatform.mcVersion >= 11800) {
            exclude(group = "net.fabricmc.fabric-api", module = "fabric-structure-api-v1")
            exclude(group = "net.fabricmc.fabric-api", module = "fabric-tag-extensions-v0")
            exclude(group = "net.fabricmc.fabric-api", module = "fabric-mining-levels-v0")
            exclude(group = "net.fabricmc.fabric-api", module = "fabric-tool-attribute-api-v1")
        }
    }
}

val mod_name: String by project
val mod_version: String by project
val mod_id: String by project
val mod_description: String by project
val mod_license: String by project
val mod_authors: String by project

version = mod_version
group = "com.mikolajkolek.fixaltgr"

blossom {
    replaceToken("@NAME@", mod_name)
    replaceToken("@ID@", mod_id)
    replaceToken("@VERSION@", mod_version)
    replaceToken("@AUTHORS@", mod_authors)
}

preprocess {
    vars.put("MC", modPlatform.mcVersion)
    vars.put("FABRIC", if (modPlatform.isFabric) 1 else 0)
    vars.put("FORGE", if (modPlatform.isForge) 1 else 0)
    vars.put("NEOFORGE", if (modPlatform.isNeoForge) 1 else 0)
    vars.put("FORGELIKE", if (modPlatform.isForgeLike) 1 else 0)
}

loom {
    if (modPlatform.isForge) forge {
        mixinConfig("$mod_id.mixins.json")
        mixinConfig("$mod_id-common.mixins.json")
    }
}

repositories {
    maven("https://cursemaven.com") {
        content {
            includeGroup("curse.maven")
        }
    }
    maven("https://api.modrinth.com/maven") {
        content { includeGroup("maven.modrinth") }
    }
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.minecraftforge.net")
    maven("https://maven.architectury.dev/")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
}

val shade: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:${modPlatform.mcVersionStr}")
    mappings(loom.officialMojangMappings())

    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly("org.jetbrains:annotations:24.0.0")

    val jnativehookDep = "com.github.kwhat:jnativehook:2.2.2"
    implementation(jnativehookDep)
    shade(jnativehookDep)

    val archGroup = if (modPlatform.mcVersion >= 11700) "dev.architectury" else "me.shedaniel"
    val archVersion = when (modPlatform.mcVersion) {
        11605 -> "1.32.68"
        11701 -> "2.10.12"
        11802 -> "4.11.93"
        11902 -> "6.5.85"
        11904 -> "8.2.91"
        12001 -> "9.2.14"
        12101 -> "13.0.8"
        else -> error("No arch version defined for ${modPlatform.mcVersion}")
    }

    modImplementation("$archGroup:architectury-${modPlatform.loaderStr}:$archVersion")

    if (modPlatform.isFabric) {
        modImplementation("net.fabricmc:fabric-loader:0.15.6")

        val fabricApiVersion = when (modPlatform.mcVersion) {
            11605 -> "0.42.0+1.16"
            11701 -> "0.46.1+1.17"
            11802 -> "0.76.0+1.18.2"
            11902 -> "0.77.0+1.19.2"
            11904 -> "0.81.1+1.19.4"
            12001 -> "0.92.2+1.20.1"
            12101 -> "0.104.0+1.21.1"
            else -> error("No fabric api version defined")
        }
        modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion") {
            exclude(module = "fabric-structure-api-v1")
        }
    } else if (modPlatform.isForge) {
        val forgeVersion = when (modPlatform.mcVersion) {
            11605 -> "36.2.39"
            11701 -> "37.1.1"
            11802 -> "40.2.0"
            11902 -> "43.3.0"
            11904 -> "45.3.0"
            12001 -> "47.1.3"
            12101 -> "52.0.2"
            else -> error("No forge version defined for ${modPlatform.mcVersion}")
        }
        "forge"("net.minecraftforge:forge:${modPlatform.mcVersionStr}-$forgeVersion")
    } else if (modPlatform.isNeoForge) {
        val neoforgeVersion = when (modPlatform.mcVersion) {
            12101 -> "21.1.65"
            else -> error("No neoforge version defined for ${modPlatform.mcVersion}")
        }
        "neoForge"("net.neoforged:neoforge:$neoforgeVersion")
    }
}

val fabricMcVersionRange = when (modPlatform.mcVersion) {
    11605 -> ">=1.14 <=1.16.5"
    11701 -> ">=1.17 <=1.17.1"
    11802 -> ">=1.18 <=1.18.2"
    11902 -> ">=1.19 <=1.19.2"
    11904 -> ">=1.19.3 <=1.19.4"
    12001 -> ">=1.20 <=1.20.1"
    12101 -> ">=1.21 <1.22"
    else -> error("No supported fabric version range defined for ${modPlatform.mcVersion}")
}

val forgeMcVersionRange = when (modPlatform.mcVersion) {
    11605 -> "[1.14,1.16.5]"
    11701 -> "[1.17,1.17.1]"
    11802 -> "[1.18,1.18.2]"
    11902 -> "[1.19,1.19.2]"
    11904 -> "[1.19.3,1.19.4]"
    12001 -> "[1.20,1.20.1]"
    12101 -> "[1.21,1.22)"
    else -> error("No supported forge version range defined for ${modPlatform.mcVersion}")
}

val prettyVersionRange = when (modPlatform.mcVersion) {
    11605 -> "1.14-1.16.5"
    11701 -> "1.17.X"
    11802 -> "1.18.X"
    11902 -> "1.19.0-1.19.2"
    11904 -> "1.19.3-1.19.4"
    12001 -> "1.20-1.20.1"
    12101 -> "1.21.X"
    else -> error("No pretty version defined for ${modPlatform.mcVersion}")
}

base.archivesName = "${mod_name.replace(":", " -")} ($prettyVersionRange-${modPlatform.loaderStr})"

tasks {
    compileJava {
        val javaRelease = if (modPlatform.mcVersion >= 12005) 21 else if (modPlatform.mcVersion >= 11800) 17 else if (modPlatform.mcVersion >= 11700) 16 else 8
        options.release.set(javaRelease)
    }

    processResources {
        val properties = mapOf(
            "id" to mod_id,
            "name" to mod_name,
            "version" to mod_version,
            "description" to mod_description,
            "authors" to mod_authors,
            "mcVersionFabric" to fabricMcVersionRange,
            "mcVersionForge" to forgeMcVersionRange,
            "license" to mod_license,
            "file" to mapOf("jarVersion" to mod_version)
        )
        inputs.properties(properties)
        filesMatching(listOf("fabric.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
            expand(properties)
        }
        if (modPlatform.isFabric) {
            exclude("META-INF/mods.toml", "META-INF/neoforge.mods.toml", "pack.mcmeta")
        } else if (modPlatform.isForge) {
            exclude("fabric.mod.json", "META-INF/neoforge.mods.toml")
        } else if (modPlatform.isNeoForge) {
            exclude("fabric.mod.json", "META-INF/mods.toml")
        }
    }

    shadowJar {
        archiveClassifier.set("dev")
        configurations = listOf(shade)
        mergeServiceFiles()
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }

    remapJar {
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        archiveBaseName.set(mod_id)
        archiveAppendix.set("${prettyVersionRange}-${modPlatform.loaderStr}")
        archiveVersion.set(mod_version.toString())
        archiveClassifier.set("")
        finalizedBy("copyJar")
    }

    register<Copy>("copyJar") {
        File("${project.rootDir}/jars").mkdir()
        from(remapJar.get().archiveFile)
        into("${project.rootDir}/jars")
        rename { _ -> "${mod_id}-${prettyVersionRange}-${modPlatform.loaderStr}-${mod_version}.jar" }
    }

    clean { delete("${project.rootDir}/jars") }
}

val skippedJars = rootProject.extra["modrinthSkippedJars"] as? List<*> ?: emptyList<Any>()
tasks.withType<com.modrinth.minotaur.TaskModrinthUpload>().configureEach {
    onlyIf { !skippedJars.contains(project.name) }
}

val modrinthMcVersions = when (modPlatform.mcVersion) {
    11605 -> listOf("1.14", "1.14.1", "1.14.2", "1.14.3", "1.14.4", "1.15", "1.15.1", "1.15.2", "1.16", "1.16.1", "1.16.2", "1.16.3", "1.16.4", "1.16.5")
    11701 -> listOf("1.17", "1.17.1")
    11802 -> listOf("1.18", "1.18.1", "1.18.2")
    11902 -> listOf("1.19", "1.19.1", "1.19.2")
    11904 -> listOf("1.19.3", "1.19.4")
    12001 -> listOf("1.20", "1.20.1")
    12101 -> listOf("1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4")
    else -> listOf(modPlatform.mcVersionStr)
}

modrinth {
    token.set(rootProject.extra["modrinthToken"].toString())
    projectId.set("fix-alt-gr")
    versionName.set("Fix Alt Gr $mod_version ${modPlatform.loader.name} $prettyVersionRange")
    versionNumber.set("$mod_version-${modPlatform.loaderStr}-$prettyVersionRange")
    versionType.set(rootProject.extra["modrinthReleaseType"].toString())
    changelog.set(rootProject.extra["modrinthChangelog"].toString())
    uploadFile.set(tasks.remapJar)

    gameVersions.addAll(modrinthMcVersions)
    loaders.add(modPlatform.loaderStr)

    dependencies {
        required.project("architectury-api")
    }
}

data class Platform(
    val mcMajor: Int,
    val mcMinor: Int,
    val mcPatch: Int,
    val loader: Loader
) {
    val mcVersion = mcMajor * 10000 + mcMinor * 100 + mcPatch
    val mcVersionStr = listOf(mcMajor, mcMinor, mcPatch).dropLastWhile { it == 0 }.joinToString(".")
    val loaderStr = loader.toString().lowercase()

    val isFabric = loader == Loader.Fabric
    val isForge = loader == Loader.Forge
    val isNeoForge = loader == Loader.NeoForge
    val isForgeLike = loader == Loader.Forge || loader == Loader.NeoForge
    val isLegacy = mcVersion <= 11202

    override fun toString(): String {
        return "$mcVersionStr-$loaderStr"
    }

    enum class Loader {
        Fabric,
        Forge,
        NeoForge
    }

    companion object {
        fun of(project: Project): Platform {
            val (versionStr, loaderStr) = project.name.split("-", limit = 2)
            val (major, minor, patch) = versionStr.split('.').map { it.toInt() } + listOf(0)
            val loader = Loader.values().first { it.name.lowercase() == loaderStr.lowercase() }
            return Platform(major, minor, patch, loader)
        }
    }
}
