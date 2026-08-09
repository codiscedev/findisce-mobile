package com.findisce.mobile.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WealthDao {

    @Query("SELECT * FROM asset_categories")
    suspend fun getCategories(): List<AssetCategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<AssetCategoryEntity>)

    @Query("DELETE FROM asset_categories")
    suspend fun clearCategories()

    @Query("SELECT * FROM wealth_items")
    suspend fun getWealthItems(): List<WealthItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWealthItems(items: List<WealthItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWealthItem(item: WealthItemEntity)

    @Query("DELETE FROM wealth_items")
    suspend fun clearWealthItems()
}
