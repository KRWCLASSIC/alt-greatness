plugins {
    alias(libs.plugins.loom) apply false
    alias(libs.plugins.preprocessorRoot)
    id("com.modrinth.minotaur") version "2.8.7" apply false
}

if (gradle.startParameter.taskNames.any { it.contains("modrinth") }) {
    System.setProperty("java.awt.headless", "false")

    val tokenFile = file("modrinth.token")
    val tokenVal = if (tokenFile.exists()) tokenFile.readText().trim() else System.getenv("MODRINTH_TOKEN")
    if (tokenVal.isNullOrBlank()) {
        throw GradleException("No Modrinth token found! Create a 'modrinth.token' file in the root folder or set MODRINTH_TOKEN env var.")
    }
    extra.set("modrinthToken", tokenVal)

    val type = project.findProperty("releaseType")?.toString() ?: run {
        val options = arrayOf("release", "beta", "alpha")
        val selection = javax.swing.JOptionPane.showInputDialog(
            null,
            "Select Release Type:",
            "Modrinth Upload",
            javax.swing.JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        )
        selection?.toString() ?: "release"
    }

    val changelogInput = project.findProperty("changelog")?.toString() ?: run {
        val textArea = javax.swing.JTextArea(10, 40)
        val scrollPane = javax.swing.JScrollPane(textArea)
        val result = javax.swing.JOptionPane.showConfirmDialog(
            null,
            scrollPane,
            "Enter Changelog",
            javax.swing.JOptionPane.OK_CANCEL_OPTION,
            javax.swing.JOptionPane.PLAIN_MESSAGE
        )
        if (result == javax.swing.JOptionPane.OK_OPTION) textArea.text else ""
    }

    val modVersion = project.findProperty("mod_version")?.toString() ?: "1.0.0"

    val panel = javax.swing.JPanel()
    panel.layout = javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS)
    panel.add(javax.swing.JLabel("Ready to upload JARs to Modrinth project 'fix-alt-gr'"))
    panel.add(javax.swing.JLabel("Release Type: $type"))
    panel.add(javax.swing.Box.createRigidArea(java.awt.Dimension(0, 10)))
    panel.add(javax.swing.JLabel("Select JARs to upload (shows Modrinth Name & File):"))

    val checkBoxes = mutableMapOf<String, javax.swing.JCheckBox>()
    subprojects.sortedBy { it.name }.forEach { sub ->
        val parts = sub.name.split("-", limit = 2)
        if (parts.size >= 2) {
            val (versionStr, loader) = parts
            val loaderCap = loader.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
            val verParts = versionStr.split('.').map { it.toInt() } + listOf(0)
            val mcVersion = verParts[0] * 10000 + verParts[1] * 100 + verParts[2]
            val prettyRange = when (mcVersion) {
                11605 -> "1.14-1.16.5"
                11701 -> "1.17.X"
                12001 -> "1.18-1.20.4"
                12101 -> "1.20.5-1.21.X"
                else -> versionStr
            }
            val versionName = "Fix Alt Gr $modVersion $loaderCap $prettyRange"
            val versionNumber = "$modVersion-$loader-$prettyRange"
            val jarFileName = "fixaltgr-$prettyRange-$loader-$modVersion.jar"
            val cb = javax.swing.JCheckBox("$versionName  |  [File: $jarFileName, Ver: $versionNumber]")
            cb.isSelected = true
            checkBoxes[sub.name] = cb
            panel.add(cb)
        }
    }

    panel.add(javax.swing.Box.createRigidArea(java.awt.Dimension(0, 10)))
    panel.add(javax.swing.JLabel("Changelog:"))
    val clArea = javax.swing.JTextArea(6, 40)
    clArea.text = changelogInput
    clArea.isEditable = false
    clArea.font = java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12)
    panel.add(javax.swing.JScrollPane(clArea))

    val finalConfirm = javax.swing.JOptionPane.showConfirmDialog(
        null,
        panel,
        "Confirm Modrinth Upload Details",
        javax.swing.JOptionPane.OK_CANCEL_OPTION,
        javax.swing.JOptionPane.PLAIN_MESSAGE
    )

    if (finalConfirm != javax.swing.JOptionPane.OK_OPTION) {
        throw GradleException("Modrinth upload cancelled by user.")
    }

    val skipped = checkBoxes.filter { !it.value.isSelected }.map { it.key }
    extra.set("modrinthSkippedJars", skipped)
    extra.set("modrinthReleaseType", type)
    extra.set("modrinthChangelog", changelogInput)
} else {
    extra.set("modrinthSkippedJars", emptyList<String>())
    extra.set("modrinthToken", "")
    extra.set("modrinthReleaseType", "release")
    extra.set("modrinthChangelog", "")
}

preprocess {
    strictExtraMappings.set(false)
    val fabric12001 = createNode("1.20.1-fabric", 12001, "mojmap") // Parent project

    val forge12001 = createNode("1.20.1-forge", 12001, "mojmap")
    forge12001.link(fabric12001)

    val fabric11701 = createNode("1.17.1-fabric", 11701, "mojmap")
    fabric11701.link(fabric12001)

    val forge11701 = createNode("1.17.1-forge", 11701, "mojmap")
    forge11701.link(fabric11701)

    val fabric11605 = createNode("1.16.5-fabric", 11605, "mojmap")
    fabric11605.link(fabric11701)

    val forge11605 = createNode("1.16.5-forge", 11605, "mojmap")
    forge11605.link(fabric11605)

    val fabric12101 = createNode("1.21.1-fabric", 12101, "mojmap")
    fabric12101.link(fabric12001)

    val neoforge12101 = createNode("1.21.1-neoforge", 12101, "mojmap")
    neoforge12101.link(fabric12101)
}

val zipJars = tasks.register<Zip>("zipJars") {
    group = "build"
    description = "Builds all subproject JARs and zips them into jars/jars.zip"
    archiveFileName.set("jars.zip")
    destinationDirectory.set(file("jars"))
    from(fileTree("jars") {
        include("fixaltgr-*.jar")
        exclude("jars.zip")
    })
    dependsOn(subprojects.map { "${it.path}:copyJar" })
}

listOf("buildJars", "jars", "build_jars", "packageJars", "copyJar").forEach { taskName ->
    tasks.register(taskName) {
        group = "build"
        description = "Alias for zipJars - Builds all FixAltGr JARs and creates jars/jars.zip"
        dependsOn(zipJars)
    }
}

gradle.projectsEvaluated {
    val sortedProjects = subprojects.sortedBy { it.name }
    for (i in 0 until sortedProjects.size - 1) {
        val current = sortedProjects[i]
        val next = sortedProjects[i + 1]
        next.tasks.named("modrinth").configure {
            mustRunAfter(current.tasks.named("modrinth"))
        }
    }
}
