package com.findisce.mobile.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asset_categories")
data class AssetCategoryEntity(
    @PrimaryKey val id: String,
    val userId: String?,
    val name: String,
    val isAppreciation: Boolean,
    val rate: Double
)
