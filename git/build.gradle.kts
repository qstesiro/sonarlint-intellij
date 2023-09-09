plugins {
    kotlin("jvm")
}

val intellijBuildVersion: String by project
val sonarlintCoreVersion: String by project

intellij {
    version.set(intellijBuildVersion)
    plugins.set(listOf("git4idea"))
}

tasks.buildSearchableOptions {
    enabled = false
}

dependencies {
    implementation(project(":common"))
    // implementation("org.sonarsource.sonarlint.core:sonarlint-core:$sonarlintCoreVersion")
    implementation("org.sonarsource.sonarlint.core:sonarlint-core:8.13-SNAPSHOT") // ???
}
