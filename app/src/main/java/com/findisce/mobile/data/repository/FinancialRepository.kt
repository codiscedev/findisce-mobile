package com.findisce.mobile.data.repository

import android.content.Context
import com.findisce.mobile.data.api.RetrofitClient
import com.findisce.mobile.data.local.db.AppDatabase
import com.findisce.mobile.data.local.db.AssetCategoryEntity
import com.findisce.mobile.data.local.db.WealthDao
import com.findisce.mobile.data.local.db.WealthItemEntity
import com.findisce.mobile.data.model.*
import java.math.BigDecimal
import java.util.UUID

class FinancialRepository(context: Context? = null) {

    private val apiService = RetrofitClient.apiService
    private val wealthDao: WealthDao? = context?.let { AppDatabase.getInstance(it.applicationContext).wealthDao() }

    suspend fun getDashboardSummary(): DashboardSummary {
        val response = apiService.getDashboardSummary()
        if (response.success && response.data != null) {
            return response.data
        } else {
            throw Exception(response.message ?: "Failed to fetch dashboard summary")
        }
    }

    suspend fun getAssetCategories(): List<AssetCategoryResponseItem> {
        // 1. Try serving from Room DB
        wealthDao?.let { dao ->
            val cached = dao.getCategories()
            if (cached.isNotEmpty()) {
                return cached.map {
                    AssetCategoryResponseItem(
                        id = it.id,
                        userId = it.userId,
                        name = it.name,
                        isAppreciation = it.isAppreciation,
                        rate = BigDecimal(it.rate)
                    )
                }
            }
        }

        // 2. Fetch from API on first run and store in Room DB
        val userId = RetrofitClient.sessionManager?.fetchUserId()
        if (!userId.isNullOrBlank()) {
            try {
                val response = apiService.getAssetCategories(userId)
                if (response.success && response.data != null) {
                    val apiList = response.data
                    val entities = apiList.map {
                        AssetCategoryEntity(
                            id = it.id ?: UUID.randomUUID().toString(),
                            userId = it.userId,
                            name = it.name ?: "Category",
                            isAppreciation = it.isAppreciation ?: true,
                            rate = it.rate?.toDouble() ?: 8.0
                        )
                    }
                    wealthDao?.insertCategories(entities)
                    return apiList
                }
            } catch (e: Exception) {
                // Return empty if API fails
            }
        }
        return emptyList()
    }

    suspend fun getDebtCategories(): List<GenericCategoryItem> {
        val userId = RetrofitClient.sessionManager?.fetchUserId()
        if (!userId.isNullOrBlank()) {
            try {
                val response = apiService.getDebtCategories(userId)
                if (response.success && response.data != null) {
                    return response.data
                }
            } catch (e: Exception) {
                // Ignore API failure
            }
        }
        return emptyList()
    }

    suspend fun getInvestmentCategories(): List<GenericCategoryItem> {
        val userId = RetrofitClient.sessionManager?.fetchUserId()
        if (!userId.isNullOrBlank()) {
            try {
                val response = apiService.getInvestmentCategories(userId)
                if (response.success && response.data != null) {
                    return response.data
                }
            } catch (e: Exception) {
                // Ignore API failure
            }
        }
        return emptyList()
    }

    suspend fun getGoalCategories(): List<GenericCategoryItem> {
        val userId = RetrofitClient.sessionManager?.fetchUserId()
        if (!userId.isNullOrBlank()) {
            try {
                val response = apiService.getGoalCategories(userId)
                if (response.success && response.data != null) {
                    return response.data
                }
            } catch (e: Exception) {
                // Ignore API failure
            }
        }
        return emptyList()
    }

    suspend fun getEssentialCategories(): List<GenericCategoryItem> {
        val userId = RetrofitClient.sessionManager?.fetchUserId()
        if (!userId.isNullOrBlank()) {
            try {
                val response = apiService.getEssentialCategories(userId)
                if (response.success && response.data != null) {
                    return response.data
                }
            } catch (e: Exception) {
                // Ignore API failure
            }
        }
        return emptyList()
    }

    suspend fun getWealthItems(): List<WealthItem> {
        // 1. Try serving from Room DB
        wealthDao?.let { dao ->
            val cached = dao.getWealthItems()
            if (cached.isNotEmpty()) {
                return cached.map {
                    WealthItem(
                        id = it.id,
                        name = it.name,
                        amount = it.amount,
                        isAsset = it.isAsset
                    )
                }
            }
        }

        // 2. Fetch from API on first run and store in Room DB
        val userId = RetrofitClient.sessionManager?.fetchUserId()
        val resultList = mutableListOf<WealthItem>()
        val entitiesToCache = mutableListOf<WealthItemEntity>()

        if (!userId.isNullOrBlank()) {
            // Fetch Assets
            try {
                val assetRes = apiService.getAssetsByUser(userId)
                if (assetRes.success && assetRes.data != null) {
                    for (asset in assetRes.data) {
                        val id = asset.id ?: UUID.randomUUID().toString()
                        val name = asset.name ?: "Asset"
                        val valAmount = asset.getEffectiveValue()
                        resultList.add(WealthItem(id, name, valAmount, true))
                        entitiesToCache.add(WealthItemEntity(id, name, valAmount, true))
                    }
                }
            } catch (e: Exception) {
                // Ignore API error
            }

            // Fetch Debts
            try {
                val debtRes = apiService.getDebtsByUser(userId)
                if (debtRes.success && debtRes.data != null) {
                    for (debt in debtRes.data) {
                        val id = debt.id ?: UUID.randomUUID().toString()
                        val name = debt.name ?: debt.loanName ?: "Debt"
                        val valAmount = debt.getEffectiveOutstanding()
                        resultList.add(WealthItem(id, name, valAmount, false))
                        entitiesToCache.add(WealthItemEntity(id, name, valAmount, false))
                    }
                }
            } catch (e: Exception) {
                // Ignore API error
            }

            if (entitiesToCache.isNotEmpty()) {
                wealthDao?.insertWealthItems(entitiesToCache)
            }
        }

        return resultList
    }

    suspend fun createAssetDetailed(
        categoryName: String,
        assetName: String,
        assetType: String,
        appreciationRate: Double,
        purchaseValue: Long,
        currentMarketValue: Long,
        propertyType: String?,
        note: String?
    ): WealthItem {
        val requestMap = mutableMapOf<String, Any?>(
            "name" to assetName,
            "type" to categoryName.uppercase().replace(" ", "_"),
            "appreciation_rate" to appreciationRate,
            "purchase_value" to purchaseValue,
            "current_value" to currentMarketValue,
            "is_appreciation" to (assetType == "APPRECIATION"),
            "note" to note
        )

        if (!propertyType.isNullOrBlank()) {
            requestMap["property_name"] = assetName
            requestMap["property_type"] = propertyType
        }

        val response = apiService.createAsset(requestMap)
        val newItem = if (response.success && response.data != null) {
            WealthItem(
                id = response.data.id ?: UUID.randomUUID().toString(),
                name = response.data.name ?: assetName,
                amount = response.data.getEffectiveValue(),
                isAsset = true
            )
        } else {
            WealthItem(
                id = UUID.randomUUID().toString(),
                name = assetName,
                amount = currentMarketValue,
                isAsset = true
            )
        }

        // Cache in Room DB
        wealthDao?.insertWealthItem(WealthItemEntity(newItem.id ?: UUID.randomUUID().toString(), newItem.name, newItem.amount, true))
        return newItem
    }

    suspend fun createAsset(name: String, amount: Long): WealthItem {
        return createAssetDetailed("OTHERS", name, "APPRECIATION", 8.0, amount, amount, null, null)
    }

    suspend fun createDebt(name: String, amount: Long): WealthItem {
        val requestMap = mapOf(
            "loanName" to name,
            "lendingBank" to "Bank",
            "sanctionedAmount" to amount,
            "outstandingPrincipal" to amount,
            "loanInterestRate" to 8.5,
            "loanTenureValue" to 12,
            "emiAmountInput" to (amount / 12)
        )
        val response = apiService.createDebt(requestMap)
        val newItem = if (response.success && response.data != null) {
            WealthItem(
                id = response.data.id ?: UUID.randomUUID().toString(),
                name = response.data.name ?: response.data.loanName ?: name,
                amount = response.data.getEffectiveOutstanding(),
                isAsset = false
            )
        } else {
            WealthItem(
                id = UUID.randomUUID().toString(),
                name = name,
                amount = amount,
                isAsset = false
            )
        }

        // Cache in Room DB
        wealthDao?.insertWealthItem(WealthItemEntity(newItem.id ?: UUID.randomUUID().toString(), newItem.name, newItem.amount, false))
        return newItem
    }

    suspend fun createInvestment(name: String, amount: Long) {
        val requestMap = mapOf(
            "name" to name,
            "type" to "EQUITY",
            "investedAmount" to amount,
            "currentValue" to amount
        )
        try { apiService.createInvestment(requestMap) } catch (e: Exception) {}
        wealthDao?.insertWealthItem(WealthItemEntity(UUID.randomUUID().toString(), name, amount, true))
    }

    suspend fun createGoal(name: String, amount: Long) {
        val requestMap = mapOf(
            "name" to name,
            "targetAmount" to amount,
            "savedAmount" to 0
        )
        try { apiService.createGoal(requestMap) } catch (e: Exception) {}
    }

    suspend fun createEssential(name: String, amount: Long) {
        val requestMap = mapOf(
            "policyName" to name,
            "sumAssured" to amount
        )
        try { apiService.createEssential(requestMap) } catch (e: Exception) {}
    }

    suspend fun getTransactions(): List<TransactionItem> {
        val userId = RetrofitClient.sessionManager?.fetchUserId()
        if (!userId.isNullOrBlank()) {
            try {
                val response = apiService.getTransactionsByUser(userId)
                if (response.success && response.data != null) {
                    return response.data.map { map ->
                        TransactionItem(
                            id = map["id"]?.toString(),
                            description = map["merchant"]?.toString() ?: map["note"]?.toString() ?: "Transaction",
                            amount = (map["amount"] as? Number)?.toLong() ?: 0L,
                            isExpense = (map["type"]?.toString() ?: "EXPENSE").equals("EXPENSE", ignoreCase = true),
                            categoryName = map["categoryName"]?.toString() ?: "Miscellaneous",
                            date = map["transactionDate"]?.toString()
                        )
                    }
                }
            } catch (e: Exception) {
                // Return empty list if network/API fails
            }
        }
        return emptyList()
    }

    suspend fun addTransaction(item: TransactionItem): TransactionItem {
        val requestMap = mapOf<String, Any?>(
            "amount" to item.amount,
            "type" to if (item.isExpense) "EXPENSE" else "INCOME",
            "merchant" to item.description,
            "note" to item.description,
            "categoryName" to (item.categoryName ?: "Miscellaneous")
        )
        val response = apiService.createTransaction(requestMap)
        if (response.success && response.data != null) {
            val map = response.data
            return TransactionItem(
                id = map["id"]?.toString(),
                description = map["merchant"]?.toString() ?: item.description,
                amount = (map["amount"] as? Number)?.toLong() ?: item.amount,
                isExpense = item.isExpense,
                categoryName = map["categoryName"]?.toString() ?: item.categoryName,
                date = map["transactionDate"]?.toString()
            )
        } else {
            return item
        }
    }
}
