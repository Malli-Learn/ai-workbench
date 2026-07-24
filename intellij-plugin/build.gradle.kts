plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaUltimate(providers.gradleProperty("platformVersion").get())
        pluginVerifier()
        zipSigner()
    }
    implementation("com.google.code.gson:gson:${providers.gradleProperty("gsonVersion").get()}")
    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    buildSearchableOptions = false
    instrumentCode = false

    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion")
        ideaVersion {
            sinceBuild = providers.gradleProperty("platformSinceBuild")
        }
    }
    
    pluginVerification {
        freeArgs.addAll("-mute", "TemplateWordInPluginId")
        ides {
            create(providers.gradleProperty("platformType").get(), providers.gradleProperty("platformVersion"))
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 21
        options.compilerArgs.add("-parameters")
    }

    test {
        useJUnit()
        doFirst {
            jvmArgs = (jvmArgs ?: mutableListOf()).filterNot { it.startsWith("-javaagent:") }.toMutableList()
        }
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
