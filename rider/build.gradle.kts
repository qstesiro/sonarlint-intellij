val sonarlintCoreVersion: String by project
val riderBuildVersion: String by project

plugins {
    kotlin("jvm")
}

intellij {
    version.set(riderBuildVersion)
}

tasks.buildSearchableOptions {
    enabled = false
}

dependencies {
    implementation(project(":common"))
    // implementation("org.sonarsource.sonarlint.core:sonarlint-core:$sonarlintCoreVersion")
    implementation("org.sonarsource.sonarlint.core:sonarlint-core:8.13-SNAPSHOT") // ???
}
