package com.findisce.mobile.data.api

import com.findisce.mobile.data.model.*
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: Map<String, String>): ApiResponse<AuthResponse>

    @POST("auth/signup")
    suspend fun signup(@Body request: Map<String, String>): ApiResponse<AuthResponse>

    @GET("v1/dashboard/summary")
    suspend fun getDashboardSummary(): ApiResponse<DashboardSummary>

    // Category Endpoints matching finone-client
    @GET("v1/assetcategory/{userId}")
    suspend fun getAssetCategories(@Path("userId") userId: String): ApiResponse<List<AssetCategoryResponseItem>>

    @GET("v1/debtcategory/{userId}")
    suspend fun getDebtCategories(@Path("userId") userId: String): ApiResponse<List<GenericCategoryItem>>

    @GET("v1/investmentcategory/{userId}")
    suspend fun getInvestmentCategories(@Path("userId") userId: String): ApiResponse<List<GenericCategoryItem>>

    @GET("v1/goalcategory/users/{userId}")
    suspend fun getGoalCategories(@Path("userId") userId: String): ApiResponse<List<GenericCategoryItem>>

    @GET("v1/essentialcategory/users/{userId}")
    suspend fun getEssentialCategories(@Path("userId") userId: String): ApiResponse<List<GenericCategoryItem>>

    // Asset Endpoints
    @GET("v1/asset/users/{userId}")
    suspend fun getAssetsByUser(@Path("userId") userId: String): ApiResponse<List<AssetResponseItem>>

    @POST("v1/asset")
    suspend fun createAsset(@Body request: Map<String, Any?>): ApiResponse<AssetResponseItem>

    // Debt Endpoints
    @GET("v1/debt/users/{userId}")
    suspend fun getDebtsByUser(@Path("userId") userId: String): ApiResponse<List<DebtResponseItem>>

    @POST("v1/debt")
    suspend fun createDebt(@Body request: Map<String, Any?>): ApiResponse<DebtResponseItem>

    // Goal Endpoints
    @GET("v1/goal/users/{userId}")
    suspend fun getGoalsByUser(@Path("userId") userId: String): ApiResponse<List<Any>>

    @POST("v1/goal")
    suspend fun createGoal(@Body request: Map<String, Any?>): ApiResponse<Any>

    // Essential Endpoints
    @GET("v1/essential/users/{userId}")
    suspend fun getEssentialsByUser(@Path("userId") userId: String): ApiResponse<List<Any>>

    @POST("v1/essential")
    suspend fun createEssential(@Body request: Map<String, Any?>): ApiResponse<Any>

    // Investment Endpoints
    @GET("v1/investment/users/{userId}")
    suspend fun getInvestmentsByUser(@Path("userId") userId: String): ApiResponse<List<Any>>

    @POST("v1/investment")
    suspend fun createInvestment(@Body request: Map<String, Any?>): ApiResponse<Any>

    // Money Flow / Transaction Endpoints (v1)
    @GET("v1/transaction/users/{userId}")
    suspend fun getTransactionsByUser(@Path("userId") userId: String): ApiResponse<List<Map<String, Any?>>>

    @POST("v1/transaction")
    suspend fun createTransaction(@Body request: Map<String, Any?>): ApiResponse<Map<String, Any?>>

    @GET("v1/income/users/{userId}")
    suspend fun getIncomesByUser(@Path("userId") userId: String): ApiResponse<List<Map<String, Any?>>>

    @GET("v1/bill/users/{userId}")
    suspend fun getBillsByUser(@Path("userId") userId: String): ApiResponse<List<Map<String, Any?>>>

    @GET("v1/budget/users/{userId}")
    suspend fun getBudgetsByUser(@Path("userId") userId: String): ApiResponse<List<Map<String, Any?>>>

    @GET("v1/creditcard/users/{userId}")
    suspend fun getCreditCardsByUser(@Path("userId") userId: String): ApiResponse<List<Map<String, Any?>>>

    // Legacy Transactions
    @GET("transactions")
    suspend fun getTransactions(): ApiResponse<List<TransactionItem>>

    @POST("transactions")
    suspend fun addTransaction(@Body item: TransactionItem): ApiResponse<TransactionItem>
}
