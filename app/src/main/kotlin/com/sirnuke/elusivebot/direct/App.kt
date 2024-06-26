/**
 * Entry point for Direct service (main function).
 */

package com.sirnuke.elusivebot.direct

import com.uchuhimo.konf.Config
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.KStream
import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

import kotlin.concurrent.thread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

@Suppress("TOO_LONG_FUNCTION")
fun main() = runBlocking {
    val log = LoggerFactory.getLogger("com.sirnuke.elusivebot.direct.AppKt")
    val config = Config { addSpec(DirectSpec) }.from.env()

    val running = AtomicBoolean(true)

    log.info("Starting Direct service with producer {} & consumer {}", config[DirectSpec.Kafka.producerTopic],
        config[DirectSpec.Kafka.consumerTopic])

    val instances: ConcurrentHashMap<String, Instance> = ConcurrentHashMap()

    val consumerConfig = StreamsConfig(
        mapOf<String, Any>(
            StreamsConfig.APPLICATION_ID_CONFIG to config[DirectSpec.serviceId],
            StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to config[DirectSpec.Kafka.bootstrap],
            StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String().javaClass.name,
            StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to Serdes.String().javaClass.name
        )
    )

    val builder = StreamsBuilder()

    val consumer: KStream<String, String> = builder.stream(config[DirectSpec.Kafka.consumerTopic])

    consumer.foreach { key, message ->
        log.info("Got response {} {}", key, message)
        this.launch { instances[key]?.onReceive(Json.decodeFromString(message)) }
    }

    val streams = KafkaStreams(builder.build(), consumerConfig)
    streams.start()

    val producerConfig = mapOf(
        "bootstrap.servers" to config[DirectSpec.Kafka.bootstrap],
        "key.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
        "value.serializer" to "org.apache.kafka.common.serialization.StringSerializer"
    )

    val producer: KafkaProducer<String, String> = KafkaProducer(producerConfig)

    val selectorManager = SelectorManager(Dispatchers.IO)
    val serverSocket = aSocket(selectorManager).tcp().bind(
        config[DirectSpec.Listen.host], config[DirectSpec.Listen.port]
    )
    log.info("Listening on {}", serverSocket.localAddress)

    Runtime.getRuntime().addShutdownHook(thread(start = false, name = "shutdown-hook") {
        running.set(false)
        serverSocket.close()
        selectorManager.close()
        streams.close()
        producer.close()
        // TODO: Close individual instances?
    })

    while (running.get()) {
        val socket = serverSocket.accept()
        val instance = Instance(config, socket, producer)
        log.info("Accepted new socket {}", socket)
        instances[instance.id] = instance
        instance.start()
    }
}
