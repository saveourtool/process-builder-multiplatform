rootProject.name = "process-builder-multiplatform"

run {
    @Suppress("UnstableApiUsage")
    dependencyResolutionManagement {
        repositories {
            mavenCentral()
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

include("core")

gradleEnterprise {
    if (System.getenv("CI") != null) {
        buildScan {
            publishAlways()
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
        }
    }
}
