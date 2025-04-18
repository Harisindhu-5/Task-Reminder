package com.example.taskit.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StringListConverter {
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return gson.toJson(list)
    }
    
    @TypeConverter
    fun toStringList(json: String?): List<String>? {
        if (json == null) return null
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }
}

class IntListConverter {
    private val gson = Gson()
    
    @TypeConverter
    fun fromIntList(list: List<Int>?): String? {
        return gson.toJson(list)
    }
    
    @TypeConverter
    fun toIntList(json: String?): List<Int>? {
        if (json == null) return null
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(json, type)
    }
} 