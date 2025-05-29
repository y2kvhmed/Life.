package com.life.app.data.local.converters

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Room type converter for LocalDateTime objects.
 * This allows Room to store LocalDateTime objects in the database as strings.
 */
class DateTimeConverter {
    
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    /**
     * Converts a LocalDateTime object to a string for storage in the database.
     * 
     * @param dateTime The LocalDateTime to convert
     * @return The string representation of the LocalDateTime, or null if the input is null
     */
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(formatter)
    }
    
    /**
     * Converts a string from the database back to a LocalDateTime object.
     * 
     * @param value The string representation of the LocalDateTime
     * @return The LocalDateTime object, or null if the input is null
     */
    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, formatter) }
    }
}