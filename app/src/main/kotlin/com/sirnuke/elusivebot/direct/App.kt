package com.sirnuke.elusivebot.direct

import com.uchuhimo.konf.Config
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    val L = LoggerFactory.getLogger("com.sirnuke.elusivebot.direct")
    val config = Config { addSpec(DirectSpec) }
        .from.env()

    runBlocking {
        // TODO: Open connection to Redis and RabbitMQ here

        val selectorManager = SelectorManager(Dispatchers.IO)
        val serverSocket = aSocket(selectorManager).tcp().bind(config[DirectSpec.Listen.host], config[DirectSpec.Listen.port])
        L.info("Listening on {}", serverSocket.localAddress)

        while (true) {
            val socket = serverSocket.accept()
            L.info("Accepted new socket {}", socket)
            // TODO: Create support structure in Redis
            // TODO: Open listeners for new messages in different scope
            // TODO: Spin this out into a separate class?
            launch {
                val receiveChannel = socket.openReadChannel()
                val sendChannel = socket.openWriteChannel(autoFlush = true)
                try {
                    while (true) {
                        val message = receiveChannel.readUTF8Line()
                        // TODO: Send to Redis
                    }
                } catch (e: Throwable) {
                    // TODO: Clean up Redis, shut it ALL DOWN
                    socket.close()
                }
            }
        }
    }
}