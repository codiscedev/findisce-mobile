package com.findisce.mobile.sms

import android.content.Context
import android.database.Cursor
import android.provider.Telephony
import java.util.Date

/**
 * One-time (or pull-to-refresh) scan of the existing SMS inbox to backfill
 * transaction history — useful right after the user grants SMS permission,
 * so they don't have to wait for new SMS to populate their transactions page.
 */
object SmsHistoryReader {

    /**
     * @param monthsBack how far back to scan (default 6 months). Pass null for all-time.
     */
    fun readHistoricalTransactions(context: Context, monthsBack: Int? = 6): List<Transaction> {
        val transactions = mutableListOf<Transaction>()

        val projection = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        var selection: String? = null
        var selectionArgs: Array<String>? = null
        if (monthsBack != null) {
            val cutoffMillis = System.currentTimeMillis() - (monthsBack * 30L * 24 * 60 * 60 * 1000)
            selection = "${Telephony.Sms.DATE} >= ?"
            selectionArgs = arrayOf(cutoffMillis.toString())
        }

        val cursor: Cursor? = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${Telephony.Sms.DATE} DESC"
        )

        cursor?.use {
            val addressIdx = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIdx = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIdx = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

            while (it.moveToNext()) {
                val sender = it.getString(addressIdx) ?: continue
                val body = it.getString(bodyIdx) ?: continue
                val date = Date(it.getLong(dateIdx))

                BankSmsParser.parse(sender, body, date)?.let { txn ->
                    transactions.add(txn)
                }
            }
        }

        return transactions
    }
}