/**
 * Entry point for Direct service (main function).
 */

package com.sirnuke.elusivebot.direct

import com.sirnuke.elusivebot.common.Kafka
import com.sirnuke.elusivebot.schema.ChatMessage
import com.uchuhimo.konf.Config
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

import kotlin.concurrent.thread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

@Suppress("TOO_LONG_FUNCTION")
fun main() = runBlocking {
    val log = LoggerFactory.getLogger("com.sirnuke.elusivebot.direct.AppKt")
    val config = Config { addSpec(DirectSpec) }.from.env()

    val running = AtomicBoolean(true)

    log.info(
        "Starting Direct service with producer {} & consumer {}", config[DirectSpec.Kafka.producerTopic],
        config[DirectSpec.Kafka.consumerTopic]
    )

    val instances: ConcurrentHashMap<String, Instance> = ConcurrentHashMap()

    val kafka = Kafka.Builder(
        applicationId = config[DirectSpec.serviceId],
        bootstrap = config[DirectSpec.Kafka.bootstrap],
        scope = this,
    ).registerConsumer(config[DirectSpec.Kafka.consumerTopic]) { key, msg: ChatMessage ->
        log.info("Got response {} {}", key, msg)
        instances[msg.header.serverId]?.onReceive(msg)
    }.construct()

    val selectorManager = SelectorManager(Dispatchers.IO)
    val serverSocket = aSocket(selectorManager).tcp().bind(
        config[DirectSpec.Listen.host], config[DirectSpec.Listen.port]
    )
    log.info("Listening on {}", serverSocket.localAddress)

    Runtime.getRuntime().addShutdownHook(thread(start = false, name = "shutdown-hook") {
        running.set(false)
        serverSocket.close()
        selectorManager.close()
        kafka.close()
        // TODO: Close individual instances?
    })

    while (running.get()) {
        val socket = serverSocket.accept()
        val instance = Instance(config, socket, kafka)
        log.info("Accepted new socket {}", socket)
        instances[instance.id] = instance
        instance.start()
    }
}
