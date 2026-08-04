package com.findisce.mobile.data.model

data class TransactionItem(
    val id: String?,
    val description: String,
    val amount: Long,
    val isExpense: Boolean,
    val date: String? = null
)
