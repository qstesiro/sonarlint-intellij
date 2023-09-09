val sonarlintCoreVersion: String by project
val clionBuildVersion: String by project

intellij {
    version.set(clionBuildVersion)
    plugins.set(listOf("com.intellij.clion", "com.intellij.cidr.base", "com.intellij.cidr.lang"))
}

dependencies {
    implementation(project(":common"))
    // implementation("org.sonarsource.sonarlint.core:sonarlint-core:$sonarlintCoreVersion")
    implementation("org.sonarsource.sonarlint.core:sonarlint-core:8.13-SNAPSHOT") // ???
    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:3.10.0")
}

tasks.buildSearchableOptions {
    enabled = false
}

tasks.test {
    useJUnitPlatform()
}
