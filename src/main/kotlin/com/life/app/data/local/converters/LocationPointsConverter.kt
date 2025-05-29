package com.life.app.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room type converter for lists of location points.
 * This allows Room to store lists of location points in the database as JSON strings.
 */
class LocationPointsConverter {
    
    private val gson = Gson()
    
    /**
     * Converts a list of location points to a JSON string for storage in the database.
     * 
     * @param locationPoints The list of location points to convert
     * @return The JSON string representation of the list, or null if the input is null
     */
    @TypeConverter
    fun fromLocationPoints(locationPoints: List<String>?): String? {
        return locationPoints?.let { gson.toJson(it) }
    }
    
    /**
     * Converts a JSON string from the database back to a list of location points.
     * 
     * @param value The JSON string representation of the list
     * @return The list of location points, or an empty list if the input is null
     */
    @TypeConverter
    fun toLocationPoints(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
}