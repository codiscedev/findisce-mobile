package com.findisce.mobile.moneyflow

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.findisce.mobile.R
import com.findisce.mobile.ui.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.UUID

data class RecurringBillItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dateOfDebit: String,
    val amount: Long,
    val frequency: String,
    val category: String,
    val subcategory: String,
    val paymentMethod: String
)

class RecurringBillsFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var tvEstMonthlyTotal: TextView
    private lateinit var tvActiveBills: TextView
    private lateinit var tvYearlyCommitment: TextView
    private lateinit var llBillsTableRows: LinearLayout

    private val recurringList: MutableList<RecurringBillItem> = mutableListOf(
        RecurringBillItem("rec_1", "Netflix", "05", 199L, "Monthly", "Entertainment", "Streaming", "Credit Card"),
        RecurringBillItem("rec_2", "Spotify Family", "12", 179L, "Monthly", "Entertainment", "Music", "Credit Card"),
        RecurringBillItem("rec_3", "Prestige Properties Rent", "01", 15000L, "Monthly", "Rent", "Housing", "Net Banking"),
        RecurringBillItem("rec_4", "Jio Fiber", "15", 999L, "Monthly", "Utilities", "Internet", "UPI"),
        RecurringBillItem("rec_5", "LIC Premium", "20", 8400L, "Yearly", "Insurance", "Life", "Net Banking")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recurring_bills, container, false)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        tvEstMonthlyTotal = view.findViewById(R.id.tvEstMonthlyTotal)
        tvActiveBills = view.findViewById(R.id.tvActiveBills)
        tvYearlyCommitment = view.findViewById(R.id.tvYearlyCommitment)
        llBillsTableRows = view.findViewById(R.id.llBillsTableRows)

        val btnAddBill = view.findViewById<View>(R.id.btnAddBill)
        btnAddBill?.setOnClickListener { showAddBillDialog() }

        renderBillsUI()

        return view
    }

    private fun renderBillsUI() {
        val context = context ?: return

        val totalMonthly = recurringList.sumOf { b ->
            when (b.frequency.lowercase()) {
                "monthly" -> b.amount.toDouble()
                "yearly" -> b.amount.toDouble() / 12.0
                "daily" -> b.amount.toDouble() * 30.0
                else -> b.amount.toDouble()
            }
        }
        val activeCount = recurringList.size
        val yearlyCommitment = totalMonthly * 12.0

        tvEstMonthlyTotal.text = "₹%,d".format(totalMonthly.toLong())
        tvActiveBills.text = activeCount.toString()
        tvYearlyCommitment.text = "₹%,d".format(yearlyCommitment.toLong())

        llBillsTableRows.removeAllViews()

        for (item in recurringList) {
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                paddingStruct()
                gravity = Gravity.CENTER_VERTICAL
            }

            // 1. Name
            val tvName = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.6f)
                text = item.name
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#111827"))
            }

            // 2. Debit Day
            val tvDebitDay = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
                text = if (item.dateOfDebit.isNotBlank()) "${item.dateOfDebit}th" else "—"
                textSize = 12f
                setTextColor(Color.parseColor("#4B5563"))
            }

            // 3. Amount
            val tvAmount = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
                text = "₹%,d".format(item.amount)
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#111827"))
                gravity = Gravity.END
            }

            // 4. Frequency Badge
            val freqContainer = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
                gravity = Gravity.CENTER
            }

            val (bgColor, txtColor) = when (item.frequency.lowercase()) {
                "yearly" -> Pair("#F3E8FF", "#7E22CE")
                "daily" -> Pair("#FEF3C7", "#B45309")
                else -> Pair("#EFF6FF", "#1D4ED8") // Monthly
            }

            val tvFreqBadge = TextView(context).apply {
                text = item.frequency.uppercase()
                textSize = 9f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor(txtColor))
                background = createPillDrawable(bgColor, 6f)
                setPadding(14, 6, 14, 6)
            }
            freqContainer.addView(tvFreqBadge)

            // 5. Category
            val tvCategory = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.4f)
                text = item.category.ifBlank { "—" }
                textSize = 12f
                setTextColor(Color.parseColor("#374151"))
            }

            // 6. Sub-category
            val tvSubcategory = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.4f)
                text = item.subcategory.ifBlank { "—" }
                textSize = 12f
                setTextColor(Color.parseColor("#6B7280"))
            }

            // 7. Payment Method
            val tvPaymentMethod = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.6f)
                text = item.paymentMethod.ifBlank { "—" }
                textSize = 12f
                setTextColor(Color.parseColor("#6B7280"))
            }

            // 8. Delete Button
            val btnDelete = ImageButton(context).apply {
                layoutParams = LinearLayout.LayoutParams(40, 40)
                setImageResource(android.R.drawable.ic_menu_delete)
                setColorFilter(Color.parseColor("#9CA3AF"))
                background = null
                setOnClickListener {
                    recurringList.remove(item)
                    renderBillsUI()
                    Toast.makeText(context, "Bill deleted", Toast.LENGTH_SHORT).show()
                }
            }

            row.addView(tvName)
            row.addView(tvDebitDay)
            row.addView(tvAmount)
            row.addView(freqContainer)
            row.addView(tvCategory)
            row.addView(tvSubcategory)
            row.addView(tvPaymentMethod)
            row.addView(btnDelete)

            llBillsTableRows.addView(row)

            val divider = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                setBackgroundColor(Color.parseColor("#F3F4F6"))
            }
            llBillsTableRows.addView(divider)
        }

        // Summary / Total Row at bottom
        val totalRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            paddingStruct()
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.parseColor("#F9FAFB"))
        }

        val tvTotalLabel = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2.6f)
            text = "Monthly Total"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#111827"))
        }

        val tvTotalVal = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
            text = "₹%,d".format(totalMonthly.toLong())
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#DC2626"))
            gravity = Gravity.END
        }

        val emptyFiller = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 5.6f)
        }

        totalRow.addView(tvTotalLabel)
        totalRow.addView(tvTotalVal)
        totalRow.addView(emptyFiller)

        llBillsTableRows.addView(totalRow)
    }

    private fun showAddBillDialog() {
        val context = context ?: return
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_bill, null)

        val etBillName = view.findViewById<EditText>(R.id.etBillName)
        val etDebitDay = view.findViewById<EditText>(R.id.etDebitDay)
        val etBillAmount = view.findViewById<EditText>(R.id.etBillAmount)
        val spBillFrequency = view.findViewById<Spinner>(R.id.spBillFrequency)
        val spBillCategory = view.findViewById<Spinner>(R.id.spBillCategory)
        val etSubcategory = view.findViewById<EditText>(R.id.etSubcategory)
        val spPaymentMethod = view.findViewById<Spinner>(R.id.spPaymentMethod)
        val btnCancel = view.findViewById<View>(R.id.btnCancelModalBill)
        val btnSave = view.findViewById<View>(R.id.btnSaveModalBill)

        val frequencies = arrayOf("Monthly", "Yearly", "Daily")
        spBillFrequency.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, frequencies)

        val categories = arrayOf("Entertainment", "Rent", "Utilities", "Insurance", "Subscription", "Fitness", "Groceries", "Other")
        spBillCategory.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, categories)

        val paymentMethods = arrayOf("Credit Card", "Net Banking", "UPI", "Debit Card", "Cash", "Other")
        spPaymentMethod.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, paymentMethods)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val name = etBillName.text.toString().trim()
            val amountStr = etBillAmount.text.toString().trim()

            if (name.isEmpty()) {
                etBillName.error = "Bill name required"
                return@setOnClickListener
            }
            if (amountStr.isEmpty()) {
                etBillAmount.error = "Amount required"
                return@setOnClickListener
            }

            val amount = amountStr.toLongOrNull() ?: 0L
            val debitDay = etDebitDay.text.toString().trim().padStart(2, '0')
            val frequency = spBillFrequency.selectedItem?.toString() ?: "Monthly"
            val category = spBillCategory.selectedItem?.toString() ?: "Other"
            val subcategory = etSubcategory.text.toString().trim()
            val paymentMethod = spPaymentMethod.selectedItem?.toString() ?: "Credit Card"

            val newBill = RecurringBillItem(
                name = name,
                dateOfDebit = debitDay,
                amount = amount,
                frequency = frequency,
                category = category,
                subcategory = subcategory,
                paymentMethod = paymentMethod
            )

            recurringList.add(newBill)
            renderBillsUI()
            Toast.makeText(context, "Recurring bill added successfully!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun createPillDrawable(colorHex: String, cornerRadiusDp: Float): GradientDrawable {
        val density = resources.displayMetrics.density
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = cornerRadiusDp * density
            setColor(Color.parseColor(colorHex))
        }
    }

    private fun LinearLayout.paddingStruct() {
        setPadding(24, 28, 24, 28)
    }
}
