package com.findisce.mobile.sms

import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class BankSmsParserTest {

    private val now = Date()

    @Test
    fun parseHdfcDebitSms_success() {
        val sender = "VM-HDFCBK"
        val body = "Rs 1,499.00 debited from A/c **4321 at Amazon Pay on 12-AUG-26. Avl Bal: Rs 45,210.50."

        val txn = BankSmsParser.parse(sender, body, now)

        assertNotNull(txn)
        assertEquals("HDFC", txn!!.bank)
        assertEquals(1499.0, txn.amount, 0.01)
        assertEquals(TransactionType.DEBIT, txn.type)
        assertEquals("4321", txn.accountLast4)
        assertEquals("Amazon Pay", txn.merchant)
        assertEquals(45210.50, txn.balanceAfter, 0.01)
    }

    @Test
    fun parseSbiUpiSms_success() {
        val sender = "AD-SBIUPI"
        val body = "Dear Customer, A/c X6789 debited by Rs.280.00 on 14Aug26 transfer to Uber India. Avl Bal Rs:12,300.00"

        val txn = BankSmsParser.parse(sender, body, now)

        assertNotNull(txn)
        assertEquals("SBI", txn!!.bank)
        assertEquals(280.0, txn.amount, 0.01)
        assertEquals(TransactionType.DEBIT, txn.type)
        assertEquals("6789", txn.accountLast4)
        assertEquals("Uber India", txn.merchant)
        assertEquals(12300.0, txn.balanceAfter, 0.01)
    }

    @Test
    fun parseIciciCreditSms_success() {
        val sender = "JM-ICICIB"
        val body = "INR 3,490.00 credited to ICICI Bank A/c XX1234 on 14-Aug-26. Available Balance is INR 89,000.00."

        val txn = BankSmsParser.parse(sender, body, now)

        assertNotNull(txn)
        assertEquals("ICICI", txn!!.bank)
        assertEquals(3490.0, txn.amount, 0.01)
        assertEquals(TransactionType.CREDIT, txn.type)
        assertEquals("1234", txn.accountLast4)
    }

    @Test
    fun parseNumericSender_emulatorFallback() {
        // Emulator sends SMS from numeric sender like "5554" or "+919876543210"
        val sender = "+919876543210"
        val body = "Rs 500 debited from A/c 9999 at Zomato. Avl Bal: Rs 1500"

        val txn = BankSmsParser.parse(sender, body, now)

        assertNotNull(txn)
        assertEquals("+919876543210", txn!!.bank) // falls back to raw sender ID
        assertEquals(500.0, txn.amount, 0.01)
        assertEquals(TransactionType.DEBIT, txn.type)
        assertEquals("9999", txn.accountLast4)
        assertEquals("Zomato", txn.merchant)
    }

    @Test
    fun parseNonTransactionalSms_ignored() {
        val otpBody = "Your OTP for HDFC Bank login is 482910. Do not share with anyone."
        val promoBody = "Win up to Rs 1000 cashback! Click here to apply for personal loan."

        assertNull(BankSmsParser.parse("VM-HDFCBK", otpBody, now))
        assertNull(BankSmsParser.parse("VM-HDFCBK", promoBody, now))
    }
}
