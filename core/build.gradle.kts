import com.saveourtool.processbuilder.configureDetekt
import com.saveourtool.processbuilder.configureDiktat

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }

    macosArm64()
    macosX64()
    linuxX64()
    mingwX64()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }

        val commonMain by getting {
            dependencies {
                api(libs.okio)

                implementation(libs.kotlin.logging)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        val commonTest by getting

        /**
         * js is supported in order to allow using ProcessBuilder in common section
         * js tests make no sense so common tests are targeted as commonNonJsTest
         * commonNonJsMain is required in order to make commonNonJsTest resolvable
         */
        val commonNonJsMain by creating {
            dependsOn(commonMain)
        }

        val commonNonJsTest by creating {
            dependsOn(commonTest)
            dependencies {
                implementation(libs.okio.fakefilesystem)
                implementation(libs.kotest.assertions.core)
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting {
            dependsOn(commonNonJsMain)
            dependencies {
                implementation(libs.slf4j)
            }
        }

        val jvmTest by getting {
            dependsOn(commonNonJsTest)
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation(libs.junit.jupiter.engine)
            }
        }

        val jsMain by getting {
            dependsOn(commonNonJsMain)
        }

        val macosArm64Main by getting
        val macosX64Main by getting
        val linuxX64Main by getting
        val mingwX64Main by getting

        val nativeMain by creating {
            dependsOn(commonNonJsMain)
            macosArm64Main.dependsOn(this)
            macosX64Main.dependsOn(this)
            linuxX64Main.dependsOn(this)
            mingwX64Main.dependsOn(this)
        }

        val macosArm64Test by getting
        val macosX64Test by getting
        val linuxX64Test by getting
        val mingwX64Test by getting

        val nativeTest by creating {
            dependsOn(commonNonJsTest)
            macosArm64Test.dependsOn(this)
            macosX64Test.dependsOn(this)
            linuxX64Test.dependsOn(this)
            mingwX64Test.dependsOn(this)
        }
    }
}

rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension> {
    lockFileDirectory = rootProject.projectDir
}

configureDetekt()
configureDiktat()
