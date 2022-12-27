import com.tmorgner.gradle.DependencyTask

plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.register<DependencyTask>("scan") {
    // onlyScanProjectModules = false

    val je = project.extensions.findByType<JavaPluginExtension>()
    if (je != null) {
        val mainSource = je.sourceSets.maybeCreate(SourceSet.MAIN_SOURCE_SET_NAME)
        classpath(mainSource.output)
    }

    testClasspath(project.configurations[JavaPlugin.TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME])
    classpath(project.configurations[JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME])
    classpath(project.configurations[JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME])
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}