package com.sirnuke.elusivebot.direct

import com.sirnuke.elusivebot.common.logger
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
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.apache.kafka.clients.producer.RecordMetadata
import java.lang.Exception

class Instance(
    private val config: Config, private val socket: Socket, private val producer: KafkaProducer<String, String>
) {
    companion object {
        val L by logger()
    }

    private val receiveChannel: ByteReadChannel = socket.openReadChannel()
    private val sendChannel: ByteWriteChannel = socket.openWriteChannel(autoFlush = true)
    private val running = AtomicBoolean(true)
    val id = "tcp-${UUID.randomUUID()}"

    suspend fun start() = coroutineScope {
        launch {
            try {
                while (running.get()) {
                    val content = receiveChannel.readUTF8Line() ?: break
                    L.info("Received '{}'", content)
                    val message = ChatMessage(
                        header = Header(serviceId = config[DirectSpec.serviceId], serverId = id), message = content
                    )
                    producer.send(
                        ProducerRecord(
                            config[DirectSpec.Kafka.producerChannel], id, Json.encodeToString(message)
                        )
                    ) { _: RecordMetadata?, e: Exception? ->
                        if (e != null)
                            L.error("Unable to send response on {}", socket, e)
                        else
                            L.info("Done sending response on {}", socket)
                    }
                }
            } catch (e: Throwable) {
                L.warn("Received error on {}", socket, e)
            } finally {
                socket.close()
            }
        }
    }

    suspend fun onReceive(message: ChatMessage) {
        sendChannel.writeStringUtf8(message.message)
    }

    fun stop() {
        // TODO: Will want some ability to interrupt the socket
        running.set(false)
    }
}