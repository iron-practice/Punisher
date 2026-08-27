plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.0.0"
    id("xyz.jpenilla.run-paper") version "3.0.0"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("com.mysql:mysql-connector-j:9.4.0")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        relocate("com.zaxxer.hikari", "xyz.ramenrrami.ironPunisher.libs.hikari")
        relocate("com.mysql", "xyz.ramenrrami.ironPunisher.libs.mysql")
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        val mcVersion = "1.21.11"
        minecraftVersion(mcVersion)
        runDirectory = rootDir.resolve("run/paper/$mcVersion")
        jvmArgs = listOf("-Dcom.mojang.eula.agree=true", "-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") { expand(props) }
    }
}
