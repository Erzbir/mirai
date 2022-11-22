/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("UnusedImport")

plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation(kotlin("gradle-plugin-api"))
    implementation(kotlin("gradle-plugin"))
    implementation(kotlin("stdlib"))

    api("com.github.jengelman.gradle.plugins:shadow:6.0.0")
    api(`jetbrains-annotations`)

    testImplementation(kotlin("test-junit5"))
    testImplementation(`junit-jupiter-api`)
    testImplementation(`junit-jupiter-params`)
    testRuntimeOnly(`junit-jupiter-engine`)
}

tasks.getByName("test", Test::class) {
    environment("mirai.root.project.dir", rootProject.projectDir.absolutePath)
    systemProperty("mirai.deps.test.must.run", System.getProperty("mirai.deps.test.must.run"))
}

val publishMiraiArtifactsToMavenLocal by tasks.registering {
    group = "mirai"
    description = "Publish all mirai artifacts to MavenLocal"
    val publishTasks = rootProject.allprojects.mapNotNull { proj ->
        proj.tasks.findByName("publishToMavenLocal")
    }
    dependsOn(publishTasks)

    doLast {
        // delete shadowed Jars, since Kotlin can't compile modules that depend on them.
        rootProject.subprojects
            .asSequence()
            .flatMap { proj -> proj.tasks.filter { task -> task.name.contains("relocate") } }
            .flatMap { it.outputs.files }
            .filter { it.isFile && it.name.endsWith(".jar") }
            .forEach { it.delete() }
    }
}

tasks.register("generateBuildConfig") {
    group = "mirai"

    doLast {
        generateBuildConfig()
    }
    tasks.getByName("testClasses").dependsOn(this)
    tasks.getByName("compileTestKotlin").dependsOn(this)
}

generateBuildConfig() // somehow "generateBuildConfig" won't execute

fun generateBuildConfig() {
    val text = """
            package net.mamoe.mirai.deps.test
            
            /**
             * This file was generated by Gradle task `generateBuildConfig`.
             */
            object BuildConfig {
                /**
                 * Kotlin version used to compile mirai-core
                 */
                const val kotlinVersion = "${Versions.kotlinCompiler}"
            }
        """.trimIndent() + "\n"
    val file = project.projectDir.resolve("test/BuildConfig.kt")
    if (!file.exists() || file.readText() != text) {
        file.writeText(text)
    }
}

// keep this property for Search Everywhere
val publishMiraiLocalArtifacts = tasks.register("publishMiraiLocalArtifacts", Exec::class) {
    group = "mirai"
    description = "Starts a child process to publish v2.99.0-deps-test artifacts to MavenLocal"

    workingDir(rootProject.projectDir)

    // The following code configures the new Gradle instance to inheriting configuration.
    // This is important especially for "mirai.target" settings
    // — On CI machines we didn't configure them to cross-compilation.
    // Note that IntelliJ listener is also inherited, so you will see normal execution feedbacks in your IDE 'Run' view.
    environment(System.getenv())
    environment("mirai.build.project.version", "2.99.0-deps-test")
    environment("mirai.target", getMiraiTargetFromGradle())

    val projectProperties =
//        gradle.startParameter.projectProperties
        mapOf<String, String>()
            .toMutableMap().apply {
                put("kotlin.compiler.execution.strategy", "in-process")
            }
            .map { "-P${it.key}=${it.value}" }
            .toTypedArray()

    val allowedProperties = arrayOf("org.gradle.parallel")
    val systemProperties =
//        gradle.startParameter.systemPropertiesArgs
        gradle.startParameter.systemPropertiesArgs
            .filter { it.key in allowedProperties }
            .map { "-D${it.key}=${it.value}" }
            .toTypedArray()

    commandLine(
        "./gradlew",
        publishMiraiArtifactsToMavenLocal.name,
        "--no-daemon",
        "--stacktrace",
        "--scan",
        *projectProperties,
        *systemProperties,
    ) // ignore other Gradle args

    doFirst {

        // TODO: 2022/11/22 fix tips
        logger.info(
            "[publishMiraiLocalArtifacts] Starting a Gradle daemon to run requested publishing tasks. " +
                    "Your system environment, JVM properties, and Gradle properties are inherited, " +
                    "but note that any other Gradle arguments are IGNORED!"
        )
        logger.info("[publishMiraiLocalArtifacts] Oh, and don't worry, the daemon will be stopped after the task finishes so it won't waste your memory!")
    }

    standardOutput = System.out
    errorOutput = System.err
}


version = Versions.core
