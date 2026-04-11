import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.devs.beer/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
    compileOnly("dev.lone:api-itemsadder:4.0.10")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
    implementation("net.wesjd:anvilgui:1.9.2-SNAPSHOT")
}

buildscript {
    dependencies {
        classpath("org.ow2.asm:asm:9.6")
        classpath("org.ow2.asm:asm-commons:9.6")
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21.5")
        jvmArgs("-Xms2G", "-Xmx2G", "-Dcom.mojang.eula.agree=true")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

tasks.withType<ShadowJar> {
    relocate("net.wesjd.anvilgui", "ak.ak32767.projecte.libs.anvilgui")

    archiveClassifier.set("")
}
