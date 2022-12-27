# Dependency Scanner - A gradle plugin to quickly get an overview over an existing codebase

When working with historically grown legacy code, one particular challenge every developer
faces is to figure out, which parts of the code base are used where. The manual solution to
the problem is having a good IDE and doing a lot of 'Find usages ..' all over the code.

This does not scale.

This module automates this process. It takes the complete classpath of an module, decompiles
all classes therein, and then records each use of other classes, fields and methods from
the byte code. This detects all uses, even thouse your IDE cannot see because the usage is 
in the middle of a long chained method call. 

## Usage

    tasks.register<DependencyTask>("scan") {
        val je = project.extensions.findByType<JavaPluginExtension>()
        if (je != null) {
            val mainSource = je.sourceSets.maybeCreate(SourceSet.MAIN_SOURCE_SET_NAME)
            classpath(mainSource.output)
        }
    
        testClasspath(project.configurations[JavaPlugin.TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME])
        classpath(project.configurations[JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME])
        classpath(project.configurations[JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME])
    }

The reports are always generated as a set of markdown files and
can be found in 'build/reports/dependencies/text'.
