package com.findisce.mobile.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wealth_items")
data class WealthItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val amount: Long,
    val isAsset: Boolean
)
