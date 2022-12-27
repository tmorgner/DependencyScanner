plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("dependency-scan-plugin") {
            id = "scan"
            implementationClass = "com.tmorgner.gradle.DependencyScanPlugin"
        }
    }
}


dependencies {
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.ow2.asm:asm:9.4")
}

repositories {
    mavenCentral()
}
