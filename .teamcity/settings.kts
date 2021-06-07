import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2020.2"

project {

    buildType(Build)
}

object Build : BuildType({
    name = "Build"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            name = "compile"
            goals = "clean compile"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
        }
        maven {
            name = "test"
            goals = "test"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
        }
        maven {
            name = "package"
            goals = "package"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
        }
        dockerCommand {
            name = "build image"
            commandType = build {
                source = file {
                    path = "Dockerfile"
                }
                namesAndTags = "sanjuniko/myfirst-app:%build.number%"
                commandArgs = "--pull"
            }
        }
        dockerCommand {
            name = "push image"
            commandType = push {
                namesAndTags = "sanjuniko/myfirst-app:%build.number%"
            }
        }
        step {
            name = "Deployment"
            type = "ssh-exec-runner"
            param("jetbrains.buildServer.deployer.username", "ubuntu")
            param("jetbrains.buildServer.sshexec.command", """
                docker container rm -f my-app || true
                docker run -p 8082:8080 -d --name my-app sanjuniko/myfirst-app:%build.number%
            """.trimIndent())
            param("jetbrains.buildServer.deployer.targetUrl", "3.236.16.30")
            param("jetbrains.buildServer.sshexec.authMethod", "SSH_AGENT")
        }
    }

    triggers {
        vcs {
        }
    }

    features {
        dockerSupport {
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_7"
            }
        }
    }
})
