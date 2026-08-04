package com.findisce.mobile.data.api

import com.findisce.mobile.data.model.*
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: Map<String, String>): AuthResponse

    @POST("auth/signup")
    suspend fun signup(@Body request: Map<String, String>): AuthResponse

    @GET("dashboard/summary")
    suspend fun getDashboardSummary(): DashboardSummary

    @GET("wealth")
    suspend fun getWealthItems(): List<WealthItem>

    @POST("wealth")
    suspend fun addWealthItem(@Body item: WealthItem): WealthItem

    @GET("transactions")
    suspend fun getTransactions(): List<TransactionItem>

    @POST("transactions")
    suspend fun addTransaction(@Body item: TransactionItem): TransactionItem
}
