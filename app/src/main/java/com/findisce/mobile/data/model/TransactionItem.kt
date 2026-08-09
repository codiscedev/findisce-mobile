package com.findisce.mobile.data.model

data class TransactionItem(
    val id: String?,
    val description: String,
    val amount: Long,
    val isExpense: Boolean,
    val categoryName: String? = null,
    val date: String? = null
)
