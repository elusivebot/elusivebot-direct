/**
 * Entry point for Direct service (main function).
 */

package com.sirnuke.elusivebot.direct

import com.uchuhimo.konf.Config
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

fun main() {
    val log = LoggerFactory.getLogger("com.sirnuke.elusivebot.direct.AppKt")
    val config = Config { addSpec(DirectSpec) }.from.env()

    val running = AtomicBoolean(true)

    val instances = ConcurrentHashMap<String, Instance>()

    val consumerConfig = StreamsConfig(
        mapOf<String, Any>(
            StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to config[DirectSpec.Kafka.bootstrap],
            StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String().javaClass.name,
            StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to Serdes.String().javaClass.name
        )
    )

    val builder = StreamsBuilder()

    val consumer = builder.stream<String, String>(config[DirectSpec.Kafka.consumerChannel])

    val streams = KafkaStreams(builder.build(), consumerConfig)

    val producerConfig = mapOf(
        "bootstrap.servers" to config[DirectSpec.Kafka.bootstrap],
        "key.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
        "value.serializer" to "org.apache.kafka.common.serialization.StringSerializer"
    )

    val producer = KafkaProducer<String, String>(producerConfig)

    val selectorManager = SelectorManager(Dispatchers.IO)
    val serverSocket = aSocket(selectorManager).tcp().bind(
        config[DirectSpec.Listen.host], config[DirectSpec.Listen.port]
    )
    log.info("Listening on {}", serverSocket.localAddress)

    Runtime.getRuntime().addShutdownHook(thread(name = "shutdown-hook") {
        running.set(false)
        serverSocket.close()
        selectorManager.close()
        streams.close()
        producer.close()
        // TODO: Close individual instances?
    })

    runBlocking {
        launch {
            consumer.foreach { key, message ->
                // TODO: Actually the right way to do this?
                this.launch { instances[key]?.onReceive(Json.decodeFromString(message)) }
            }
        }
        while (running.get()) {
            val socket = serverSocket.accept()
            val instance = Instance(config, socket, producer)
            log.info("Accepted new socket {}", socket)
            instances[instance.id] = instance
            instance.start()
        }
    }
}
