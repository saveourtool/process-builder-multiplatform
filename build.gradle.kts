import com.saveourtool.processbuilder.configureDetekt
import com.saveourtool.processbuilder.configureDiktat
import com.saveourtool.processbuilder.configurePublishing
import com.saveourtool.processbuilder.configureVersioning
import com.saveourtool.processbuilder.createDetektTask
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
