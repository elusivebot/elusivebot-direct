package com.sirnuke.elusivebot.direct

import com.uchuhimo.konf.ConfigSpec

object DirectSpec : ConfigSpec() {
    val listenHost by optional("0.0.0.0")
    val listenPort by required<Int>()

    val redisHost by required<String>()
    val redisPort by required<Int>()
}