package com.android.todoapp

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

/**
 *
 */
@Serializable
data class TodoTask(
    @Serializable(with = UUIDSerializer::class)
    var todoId : UUID = UUID.randomUUID(),
    var todoName : String = "",
    var todoDescription: String = "",
    var dueDate : String = Date().formatDateString()
//    var highPriority : Boolean = false,
//    var reminderStartDate : Date = Date()
)

/**
 *
 */
object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}