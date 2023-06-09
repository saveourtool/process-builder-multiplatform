import com.saveourtool.processbuilder.Versions
import com.saveourtool.processbuilder.configureDetekt
import com.saveourtool.processbuilder.configureDiktat
import com.saveourtool.processbuilder.configurePublishing

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm {
        jvmToolchain(Versions.jdk)
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    macosArm64()
    macosX64()
    linuxX64()
    mingwX64()

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }

        val commonMain by getting {
            dependencies {
                implementation(projects.core)
                api(libs.okio)
                implementation(libs.kommand)
                implementation(libs.ktor.io)

                implementation(libs.kotlin.logging)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.okio.fakefilesystem)
                implementation(libs.kotest.assertions.core)
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.slf4j)
            }
        }

        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation(libs.junit.jupiter.engine)
            }
        }

        val macosArm64Main by getting
        val macosX64Main by getting
        val linuxX64Main by getting

        val posixMain by creating {
            dependsOn(commonMain)
            macosArm64Main.dependsOn(this)
            macosX64Main.dependsOn(this)
            linuxX64Main.dependsOn(this)
        }

        val macosArm64Test by getting
        val macosX64Test by getting
        val linuxX64Test by getting

        val posixTest by creating {
            dependsOn(commonTest)
            macosArm64Test.dependsOn(this)
            macosX64Test.dependsOn(this)
            linuxX64Test.dependsOn(this)
        }

        val mingwX64Main by getting {
            dependsOn(commonMain)
        }

        val mingwX64Test by getting {
            dependsOn(commonTest)
        }
    }
}

configureDetekt()
configureDiktat()
configurePublishing()
