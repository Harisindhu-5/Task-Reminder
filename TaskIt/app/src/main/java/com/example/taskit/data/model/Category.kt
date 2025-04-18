package com.example.taskit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.taskit.data.local.converters.DateConverter
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "categories")
@TypeConverters(DateConverter::class)
data class Category(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val color: Int,
    val icon: String? = null, // Icon identifier from Material icons
    val dateCreated: LocalDateTime = LocalDateTime.now(),
    val dateModified: LocalDateTime = LocalDateTime.now()
) 