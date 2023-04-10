
import com.saveourtool.processbuilder.configureDetekt
import com.saveourtool.processbuilder.configureDiktat
import com.saveourtool.processbuilder.createDetektTask

group = "com.saveourtool"
description = "Multiplatform ProcessBuilder implementation"

plugins {
    id("org.cqfn.diktat.diktat-gradle-plugin")
    id("io.gitlab.arturbosch.detekt")
}

configureDiktat()
createDetektTask()
configureDetekt()
