package com.sirnuke.elusivebot.direct

import com.uchuhimo.konf.ConfigSpec

object DirectSpec : ConfigSpec("direct") {
    val serviceId by optional("Direct")
    object Listen : ConfigSpec() {
        val host by optional("0.0.0.0")
        val port by required<Int>()
    }

    object Kafka : ConfigSpec() {
        val bootstrap by required<String>()
        val producerTopic by optional("messages-input")
        val consumerTopic by optional("messages-output")
    }
}
