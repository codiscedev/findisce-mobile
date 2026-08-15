package com.findisce.mobile.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import java.util.Date

class SMSImportBroadcastReceiver : BroadcastReceiver()
{
    override fun onReceive(context: Context, intent: Intent)
    {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (sms in messages)
        {
            val sender = sms.originatingAddress ?: continue
            val body = sms.messageBody ?: continue
            val timestamp = Date(sms.timestampMillis)

            val transaction = BankSmsParser.parse(sender, body, timestamp)
            if (transaction != null) {
                onTransactionParsed(context, transaction)
            }
        }
    }

    private fun onTransactionParsed(context: Context, transaction: Transaction) {
        // TODO: persist via Room / your repository, then notify the UI.
        // Example: TransactionRepository.getInstance(context).insert(transaction)
        //
        // If confidence is low (< 0.6), consider queueing it for manual user
        // review/confirmation instead of auto-adding it to the transactions list.
    }
}