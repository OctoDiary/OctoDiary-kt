package org.bxkr.octodiary.database


import androidx.compose.material.icons.Icons
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        if (value == null || value.isEmpty()) return emptyList()
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
    
    @TypeConverter
    fun stringListToString(list: List<String>?): String {
        return gson.toJson(list ?: emptyList<String>())
    }
    
    @TypeConverter
    fun fromLongList(value: String?): List<Long> {
        if (value == null || value.isEmpty()) return emptyList()
        val listType = object : TypeToken<List<Long>>() {}.type
        return gson.fromJson(value, listType)
    }
    
    @TypeConverter
    fun longListToString(list: List<Long>?): String {
        return gson.toJson(list ?: emptyList<Long>())
    }
    
    @TypeConverter
    fun fromIntList(value: String?): List<Int> {
        if (value == null || value.isEmpty()) return emptyList()
        val listType = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, listType)
    }
    
    @TypeConverter
    fun intListToString(list: List<Int>?): String {
        return gson.toJson(list ?: emptyList<Int>())
    }
}



