package com.sirnuke.elusivebot.direct

import com.sirnuke.elusivebot.common.logger
import com.sirnuke.elusivebot.schema.ChatMessage
import com.sirnuke.elusivebot.schema.Header
import com.uchuhimo.konf.Config
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata

import java.lang.Exception
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * @param config
 * @param socket
 * @param producer
 */
class Instance(
    private val config: Config,
    private val socket: Socket,
    private val producer: KafkaProducer<String, String>
) {
    private val receiveChannel: ByteReadChannel = socket.openReadChannel()
    private val sendChannel: ByteWriteChannel = socket.openWriteChannel(autoFlush = true)
    private val running = AtomicBoolean(true)

    /**
     * Unique identifier for this session.  Suitable for use as a Kafka key.
     */
    val id = "tcp-${UUID.randomUUID()}"

    /**
     * Launch processing for this instance.
     *
     * @return Coroutine containing the processing loop.
     */
    suspend fun start() = coroutineScope {
        launch {
            try {
                while (running.get()) {
                    val content = receiveChannel.readUTF8Line() ?: break
                    log.info("Received '{}'", content)
                    val message = ChatMessage(
                        header = Header(serviceId = config[DirectSpec.serviceId], serverId = id), message = content
                    )
                    producer.send(
                        ProducerRecord(
                            config[DirectSpec.Kafka.producerTopic], config[DirectSpec.serviceId], Json.encodeToString(
                                message
                            )
                        )
                    ) { _: RecordMetadata?, ex: Exception? ->
                        ex?.let {
                            log.error("Unable to send response on {}", socket, ex)
                        } ?: log.info("Done sending response on {}", socket)
                    }
                }
            } catch (ex: Throwable) {
                log.warn("Received error on {}", socket, ex)
            } finally {
                socket.close()
            }
        }
    }

    /**
     * Pass an incoming message to the processing loop.
     *
     * @param message
     */
    suspend fun onReceive(message: ChatMessage) {
        sendChannel.writeStringUtf8(message.message)
    }

    /**
     * Terminate this instance.
     */
    fun stop() {
        // TODO: Will want some ability to interrupt the socket
        running.set(false)
    }
    companion object {
        val log by logger()
    }
}
