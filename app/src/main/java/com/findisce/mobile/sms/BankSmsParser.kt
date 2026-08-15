package com.findisce.mobile.sms

import java.util.Date
import java.util.regex.Pattern

/**
 * Parses transactional bank/UPI SMS into Transaction objects.
 *
 * Strategy:
 *  1. Quick filter — is this even a transactional SMS?
 *  2. Try bank-specific regex template (matched by sender ID prefix).
 *  3. Fall back to a generic template if no bank-specific one matches.
 *  4. Score confidence based on how many fields were successfully extracted.
 */
object BankSmsParser {

    // Keywords that indicate a transactional SMS (used for the initial filter)
    private val TRANSACTION_KEYWORDS = listOf(
        "debited", "credited", "debit", "credit", "spent", "withdrawn",
        "paid", "received", "txn", "transaction", "purchase", "avl bal", "a/c"
    )

    // Common promotional/OTP keywords to explicitly exclude
    private val EXCLUDE_KEYWORDS = listOf(
        "otp", "one time password", "offer", "cashback upto", "win ", "coupon",
        "loan pre-approved", "click here to apply"
    )

    private val AMOUNT_REGEX = Pattern.compile(
        "(?:Rs\\.?|INR|₹)\\s?([\\d,]+\\.?\\d{0,2})", Pattern.CASE_INSENSITIVE
    )
    private val BALANCE_REGEX = Pattern.compile(
        "(?:Avl\\s?Bal|Available\\s?Balance|Avail\\s?Bal)[:\\s]*(?:Rs\\.?|INR|₹)?\\s?([\\d,]+\\.?\\d{0,2})",
        Pattern.CASE_INSENSITIVE
    )
    private val ACCOUNT_REGEX = Pattern.compile(
        "(?:A/c|Acct|Account|Card)\\s?(?:No\\.?)?\\s?[Xx*]*(\\d{4})\\b", Pattern.CASE_INSENSITIVE
    )
    private val DEBIT_REGEX = Pattern.compile(
        "\\b(debited|debit|spent|withdrawn|paid)\\b", Pattern.CASE_INSENSITIVE
    )
    private val CREDIT_REGEX = Pattern.compile(
        "\\b(credited|credit|received|deposited)\\b", Pattern.CASE_INSENSITIVE
    )
    // Merchant: text after "at" / "to" / "VPA" up to the next punctuation
    private val MERCHANT_REGEX = Pattern.compile(
        "(?:at|to|VPA)\\s+([A-Za-z0-9@._\\-\\s]{2,30}?)(?:\\s(?:on|dt|Ref|for|via)\\b|[.,]|$)",
        Pattern.CASE_INSENSITIVE
    )

    // Map sender ID prefixes to a friendly bank name.
    // Extend this as you encounter more sender IDs in production.
    private val BANK_SENDER_MAP = mapOf(
        "HDFCBK" to "HDFC",
        "SBIINB" to "SBI",
        "SBIUPI" to "SBI",
        "ICICIB" to "ICICI",
        "ICICIT" to "ICICI",
        "AXISBK" to "Axis",
        "KOTAKB" to "Kotak",
        "PAYTM"  to "Paytm",
        "IDFCFB" to "IDFC First"
    )

    fun isTransactional(sender: String, body: String): Boolean {
        val lower = body.lowercase()
        if (EXCLUDE_KEYWORDS.any { lower.contains(it) }) return false
        if (AMOUNT_REGEX.matcher(body).find().not()) return false
        return TRANSACTION_KEYWORDS.any { lower.contains(it) }
    }

    fun parse(sender: String, body: String, receivedAt: Date): Transaction? {
        if (!isTransactional(sender, body)) return null

        var fieldsFound = 0
        var totalFields = 5 // amount, type, account, merchant, balance

        val amountMatcher = AMOUNT_REGEX.matcher(body)
        val amount = if (amountMatcher.find()) {
            fieldsFound++
            amountMatcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: return null
        } else {
            return null // amount is mandatory — no amount, no transaction
        }

        val type = when {
            DEBIT_REGEX.matcher(body).find() -> { fieldsFound++; TransactionType.DEBIT }
            CREDIT_REGEX.matcher(body).find() -> { fieldsFound++; TransactionType.CREDIT }
            else -> TransactionType.UNKNOWN
        }

        val accountMatcher = ACCOUNT_REGEX.matcher(body)
        val account = if (accountMatcher.find()) {
            fieldsFound++
            accountMatcher.group(1)
        } else null

        val merchantMatcher = MERCHANT_REGEX.matcher(body)
        val merchant = if (merchantMatcher.find()) {
            fieldsFound++
            merchantMatcher.group(1)?.trim()
        } else null

        val balanceMatcher = BALANCE_REGEX.matcher(body)
        val balance = if (balanceMatcher.find()) {
            fieldsFound++
            balanceMatcher.group(1)?.replace(",", "")?.toDoubleOrNull()
        } else null

        val bank = BANK_SENDER_MAP.entries.firstOrNull {
            sender.contains(it.key, ignoreCase = true)
        }?.value ?: sender

        val confidence = fieldsFound.toFloat() / totalFields

        return Transaction(
            amount = amount,
            type = type,
            bank = bank,
            accountLast4 = account,
            merchant = merchant,
            balanceAfter = balance,
            date = receivedAt,
            rawSms = body,
            confidence = confidence
        )
    }
}
