package com.sirnuke.elusivebot.direct

import com.sirnuke.elusivebot.schema.common.Header
import com.sirnuke.elusivebot.schema.messages.ChatMessage
import com.uchuhimo.konf.Config
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

class Instance(
    private val config: Config, private val socket: Socket, private val producer: KafkaProducer<String, String>
) {
    private val log = LoggerFactory.getLogger("com.sirnuke.elusivebot.direct.Instance")
    private val receiveChannel: ByteReadChannel = socket.openReadChannel()
    private val sendChannel: ByteWriteChannel = socket.openWriteChannel(autoFlush = true)
    private val running = AtomicBoolean(true)
    val id = "tcp-${UUID.randomUUID()}"

    suspend fun start() = coroutineScope {
        launch {
            try {
                while (running.get()) {
                    val content = receiveChannel.readUTF8Line() ?: break
                    log.info("Received '{}'", content)
                    val message = ChatMessage(
                        header = Header(serviceId = config[DirectSpec.serviceId], serverId = id), message = content
                    )
                    // TODO: Convert to JSON please
                    producer.send(ProducerRecord(config[DirectSpec.Kafka.producerChannel], 1, id, message.toString()))
                }
            } catch (e: Throwable) {
                log.warn("Received error on {}", socket, e)
            } finally {
                socket.close()
            }
        }
    }

    suspend fun onReceive(message: String) {
        // TODO: Stub!
        sendChannel.writeStringUtf8(message)
    }

    fun stop() {
        // TODO: Will want some ability to interrupt the socket
        running.set(false)
    }
}