plugins {
    kotlin("jvm")
}

val sonarlintCoreVersion: String by project
val intellijBuildVersion: String by project

intellij {
    version.set(intellijBuildVersion)
}

tasks.buildSearchableOptions {
    enabled = false
}

dependencies {
    // implementation("org.sonarsource.sonarlint.core:sonarlint-core:$sonarlintCoreVersion")
    implementation("org.sonarsource.sonarlint.core:sonarlint-core:8.13-SNAPSHOT") // ???
}
