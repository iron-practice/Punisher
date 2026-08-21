plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.0"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    runServer {
        val mcVersion = "1.21.11"
        minecraftVersion(mcVersion)
        runDirectory = rootDir.resolve("run/paper/$mcVersion")
        jvmArgs = listOf(
            "-Dcom.mojang.eula.agree=true",
            "-Xms2G",
            "-Xmx2G"
        )
        downloadPlugins {
            url("https://cdn.modrinth.com/data/Vebnzrzj/versions/b0mk8uS6/LuckPerms-Bukkit-5.5.71.jar?mr_download_reason=standalone&mr_game_version=1.21.11&mr_loader=paper")
        }
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
