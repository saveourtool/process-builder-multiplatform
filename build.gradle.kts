import com.saveourtool.processbuilder.*

group = "com.saveourtool"
description = "Kotlin Process Builder"

plugins {
    id("org.cqfn.diktat.diktat-gradle-plugin")
    id("io.gitlab.arturbosch.detekt")
}

configureVersioning()
configurePublishing()

configureDiktat()
createDetektTask()
configureDetekt()

tasks.withType<AbstractPublishToMaven> {
    dependsOn(tasks.withType<Sign>())
}
