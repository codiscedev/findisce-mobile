package com.findisce.mobile.data.repository

import com.findisce.mobile.data.api.RetrofitClient
import com.findisce.mobile.data.model.*

class FinancialRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun getDashboardSummary(): DashboardSummary {
        return apiService.getDashboardSummary()
    }

    suspend fun getWealthItems(): List<WealthItem> {
        return apiService.getWealthItems()
    }

    suspend fun addWealthItem(item: WealthItem): WealthItem {
        return apiService.addWealthItem(item)
    }

    suspend fun getTransactions(): List<TransactionItem> {
        return apiService.getTransactions()
    }

    suspend fun addTransaction(item: TransactionItem): TransactionItem {
        return apiService.addTransaction(item)
    }
}
