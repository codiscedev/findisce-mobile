package com.findisce.mobile.sms

import java.util.Date

enum class TransactionType { DEBIT, CREDIT, UNKNOWN }

data class Transaction(
    val amount: Double,
    val type: TransactionType,
    val bank: String,               // e.g. "HDFC", "SBI", "ICICI"
    val accountLast4: String?,      // last 4 digits of account/card, if found
    val merchant: String?,          // payee / merchant name, if found
    val balanceAfter: Double?,      // available balance after txn, if found
    val date: Date,                 // SMS received date (or parsed date if present)
    val rawSms: String,             // original message, kept for audit/debug
    val confidence: Float           // 0.0-1.0, how confident the parse is
)