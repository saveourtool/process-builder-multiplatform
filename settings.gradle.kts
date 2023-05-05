rootProject.name = "process-builder-multiplatform"

run {
    @Suppress("UnstableApiUsage")
    dependencyResolutionManagement {
        repositories {
            mavenCentral()
            maven {
                name = "saveourtool/okio-extras"
                url = uri("https://maven.pkg.github.com/saveourtool/okio-extras")
                credentials {
                    username = providers.gradleProperty("gpr.user").orNull
                        ?: System.getenv("GITHUB_ACTOR")
                    password = providers.gradleProperty("gpr.key").orNull
                        ?: System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.enterprise") version "3.12.6"
}

gradleEnterprise {
    if (System.getenv("CI") != null) {
        buildScan {
            publishAlways()
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("core")
include("engine-save")
// todo: include("engine-kommand")
