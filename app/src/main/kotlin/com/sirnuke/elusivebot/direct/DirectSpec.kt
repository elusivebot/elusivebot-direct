package com.sirnuke.elusivebot.direct

import com.uchuhimo.konf.ConfigSpec

object DirectSpec : ConfigSpec("direct") {
    object Listen : ConfigSpec() {
        val host by optional("0.0.0.0")
        val port by required<Int>()
    }

    object Redis : ConfigSpec() {
        val host by required<String>()
        val port by required<Int>()
    }

    object RabbitMq : ConfigSpec() {
        val host by required<String>()
        val port by required<Int>()
    }
}
