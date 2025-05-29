package com.life.app.util

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Gson TypeAdapter for converting LocalDateTime objects to and from JSON.
 */
class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun write(out: JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(formatter.format(value))
        }
    }

    override fun read(reader: JsonReader): LocalDateTime? {
        return if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull()
            null
        } else {
            LocalDateTime.parse(reader.nextString(), formatter)
        }
    }
}