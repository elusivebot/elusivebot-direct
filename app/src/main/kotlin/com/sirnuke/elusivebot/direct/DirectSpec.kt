package com.sirnuke.elusivebot.direct

import com.uchuhimo.konf.ConfigSpec

object DirectSpec : ConfigSpec("direct") {
    object Listen : ConfigSpec() {
        val host by optional("0.0.0.0")
        val port by required<Int>()
    }

    val serviceId by optional("Direct")

    object Kafka : ConfigSpec() {
        val bootstrap by required<String>()
        val producerChannel by optional("tcp-input")
        val consumerChannel by optional("tcp-output")
    }
}
