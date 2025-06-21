plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.github.samphinizy"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.yaml:snakeyaml:2.0")
    
    testImplementation("org.mockito:mockito-core:5.1.1")
    testImplementation("org.mockito:mockito-inline:5.1.1")
    testImplementation("junit:junit:4.13.2")
}

intellij {
    version.set("2023.3.7")
    type.set("IC")
    
    plugins.set(listOf("yaml"))
}

configurations.all {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
    
    withType<JavaCompile> {
        options.release.set(17)
    }
    
    test {
        systemProperty("idea.test.cyclic.buffer.size", "1048576")
        jvmArgs("-XX:+UseG1GC", "-XX:SoftRefLRUPolicyMSPerMB=50")
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    patchPluginXml {
        sinceBuild.set("233")
        untilBuild.set("251.*")
        
        // Marketplace metadata
        pluginDescription.set(
            """
            Native Taskfile.dev integration for JetBrains IDEs with automatic discovery, task execution, and comprehensive project management capabilities.
            
            Key Features:
            • Automatic Discovery: Finds all Taskfile.yml files in your project
            • Tool Window: Dedicated panel with tree view of all tasks  
            • One-Click Execution: Run tasks directly from IDE with console output
            • Global Action: Quick task runner via Ctrl+Shift+T
            • Multi-Taskfile Support: Handles projects with multiple taskfiles
            • Rich UI: Tasks with descriptions, run buttons, and directory context
            
            Getting Started:
            1. Open any project with Taskfile.yml files
            2. The Taskfile tool window appears on the right side
            3. Double-click tasks to run them or use Ctrl+Shift+T
            
            Supports all Taskfile.dev v3 features including task dependencies, descriptions, and various command formats.
            """.trimIndent()
        )
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}