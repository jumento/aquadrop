import java.net.URL

plugins {
    java
}

group = "com.aquadrop"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// Generate Manifest Task based on BOT.md constraints
val generateManifest by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/resources")
    outputs.dir(outputDir)
    
    inputs.property("group", project.group)
    inputs.property("name", rootProject.name)
    inputs.property("version", project.version)

    doLast {
        var activeServerVersion = "2026.02.18-f3b8fff95" // Fallback seguro
        try {
            val xmlText = URL("https://maven.hytale.com/release/com/hypixel/hytale/Server/maven-metadata.xml").readText()
            val regex = "<release>(.*?)</release>".toRegex()
            val match = regex.find(xmlText)
            if (match != null) {
                activeServerVersion = match.groupValues[1]
            }
        } catch (e: Exception) {
            println("Advertencia: No se pudo conectar al maven oficial de Hytale. Se usa la versión base.")
        }

        val json = """
            {
              "Group": "${project.group}",
              "Name": "AquaDrop",
              "Version": "${project.version}",
              "ServerVersion": "$activeServerVersion",
              "Main": "com.aquadrop.AquaDrop",
              "Authors": [
                  { "Name": "jume" }
              ],
              "IncludesAssetPack": true
            }
        """.trimIndent()

        outputDir.get().file("manifest.json").asFile.apply {
            parentFile.mkdirs()
            writeText(json)
        }
    }
}

sourceSets {
    main {
        resources {
            srcDir(generateManifest)
        }
    }
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Main-Class" to "com.aquadrop.AquaDrop" // Updated main class
        )
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(fileTree("libs") { include("*.jar") })
    implementation("com.google.code.gson:gson:2.10.1")
}
