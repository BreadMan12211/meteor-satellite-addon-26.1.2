plugins {
    id ("fabric-loom") version "1.10-SNAPSHOT"
    id ("com.github.johnrengelman.shadow") version "8.1.1"
}

val buildNumber: String? by project
val library: Configuration by configurations.creating

base {
    archivesName = properties["archives_base_name"] as String

    version = properties["minecraft_version"] as String
    if (buildNumber != null) {
        version = "$version-$buildNumber"
    }

    group = properties["maven_group"] as String
}

repositories {
    maven {
        name = "meteor-maven"
        url = uri("https://maven.meteordev.org/releases")
    }
    maven {
        name = "meteor-maven-snapshots"
        url = uri("https://maven.meteordev.org/snapshots")
    }
}



configurations {
    // include libraries
    implementation.configure {
        extendsFrom(library)
    }
    shadow.configure{
        extendsFrom(library)
    }
}

dependencies {
    // Fabric
    minecraft("com.mojang:minecraft:${properties["minecraft_version"] as String}")
    mappings("net.fabricmc:yarn:${properties["yarn_mappings"] as String}:v2")
    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"] as String}")

    // Meteor
    modImplementation("meteordevelopment:meteor-client:${properties["minecraft_version"] as String}-SNAPSHOT")

    // Library
    library("org.java-websocket:Java-WebSocket:1.6.0")
}

tasks {
    processResources {
        val propertyMap = mapOf(
            "version" to project.version,
            "mc_version" to project.property("minecraft_version"),
        )

        filesMatching("fabric.mod.json") {
            expand(propertyMap)
        }
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${inputs.properties["archivesName"]}" }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 21
    }

    shadowJar {
        configurations = listOf(project.configurations.shadow.get())

        from("LICENSE") {
            rename { "${it}_${project.base.archivesName.get()}" }
        }

        dependencies {
            exclude {
                it.moduleGroup == "org.slf4j"
            }
        }
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.get().archiveFile)
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}
