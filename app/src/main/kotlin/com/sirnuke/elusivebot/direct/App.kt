/**
 * Entry point for Direct service (main function).
 */

package com.sirnuke.elusivebot.direct

import com.uchuhimo.konf.Config
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.readUTF8Line
import org.slf4j.LoggerFactory

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    val log = LoggerFactory.getLogger("com.sirnuke.elusivebot.direct")
    val config = Config { addSpec(DirectSpec) }
        .from.env()

    runBlocking {
        // TODO: Open connection to Redis and RabbitMQ here

        val selectorManager = SelectorManager(Dispatchers.IO)
        val serverSocket = aSocket(selectorManager).tcp().bind(config[DirectSpec.Listen.host],
            config[DirectSpec.Listen.port])
        log.info("Listening on {}", serverSocket.localAddress)

        while (true) {
            val socket = serverSocket.accept()
            log.info("Accepted new socket {}", socket)
            // TODO: Create support structure in Redis
            // TODO: Open listeners for new messages in different scope
            // TODO: Spin this out into a separate class?
            launch {
                val receiveChannel = socket.openReadChannel()
                val sendChannel = socket.openWriteChannel(autoFlush = true)
                try {
                    while (true) {
                        val message = receiveChannel.readUTF8Line() ?: break
                        log.info("Received '{}'", message)
                        // TODO: Send to RabbitMQ
                    }
                } catch (e: Throwable) {
                    log.warn("Received error on {}", socket, e)
                } finally {
                    // TODO: Clean up Redis/RabbitMQ, shut it ALL DOWN
                    log.info("Shutting down {}", socket)
                    socket.close()
                }
            }
        }
    }
}
