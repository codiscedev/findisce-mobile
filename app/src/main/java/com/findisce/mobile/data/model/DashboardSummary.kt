package com.findisce.mobile.data.model

data class DashboardSummary(
    val estimatedNetWorth: Long,
    val totalAssets: Long,
    val totalLiabilities: Long,
    val monthlySpending: Long,
    val safetyReserve: Long
)
