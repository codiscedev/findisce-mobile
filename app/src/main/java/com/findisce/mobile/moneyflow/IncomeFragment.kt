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
import com.google.android.material.button.MaterialButton
import java.util.UUID

data class IncomeStreamItem(
    val id: String = UUID.randomUUID().toString(),
    val source: String,
    val amount: Long,
    val isFixed: Boolean,
    val fetchType: String,
    val dateOfCredit: String
)

class IncomeFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var tvTotalIncomeHeader: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvIncomeStreamsCount: TextView
    private lateinit var tvFixedIncome: TextView
    private lateinit var tvVariableIncome: TextView
    private lateinit var llIncomeTableRows: LinearLayout
    private lateinit var btnModeMonthly: MaterialButton
    private lateinit var btnModeYearly: MaterialButton

    private var isMonthlyMode: Boolean = true

    private val incomeList: MutableList<IncomeStreamItem> = mutableListOf(
        IncomeStreamItem("inc_1", "Salary", 89000L, isFixed = true, fetchType = "Manual", dateOfCredit = "2026-07-31")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_income, container, false)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        tvTotalIncomeHeader = view.findViewById(R.id.tvTotalIncomeHeader)
        tvTotalIncome = view.findViewById(R.id.tvTotalIncome)
        tvIncomeStreamsCount = view.findViewById(R.id.tvIncomeStreamsCount)
        tvFixedIncome = view.findViewById(R.id.tvFixedIncome)
        tvVariableIncome = view.findViewById(R.id.tvVariableIncome)
        llIncomeTableRows = view.findViewById(R.id.llIncomeTableRows)
        btnModeMonthly = view.findViewById(R.id.btnModeMonthly)
        btnModeYearly = view.findViewById(R.id.btnModeYearly)

        val btnAddIncome = view.findViewById<View>(R.id.btnAddIncome)
        btnAddIncome?.setOnClickListener { showAddIncomeDialog() }

        btnModeMonthly.setOnClickListener {
            if (!isMonthlyMode) {
                isMonthlyMode = true
                updateToggleUI()
                renderIncomeUI()
            }
        }

        btnModeYearly.setOnClickListener {
            if (isMonthlyMode) {
                isMonthlyMode = false
                updateToggleUI()
                renderIncomeUI()
            }
        }

        renderIncomeUI()

        return view
    }

    private fun updateToggleUI() {
        if (isMonthlyMode) {
            btnModeMonthly.setBackgroundColor(Color.WHITE)
            btnModeMonthly.setTextColor(Color.parseColor("#111827"))
            btnModeYearly.setBackgroundColor(Color.TRANSPARENT)
            btnModeYearly.setTextColor(Color.parseColor("#6B7280"))
        } else {
            btnModeYearly.setBackgroundColor(Color.WHITE)
            btnModeYearly.setTextColor(Color.parseColor("#111827"))
            btnModeMonthly.setBackgroundColor(Color.TRANSPARENT)
            btnModeMonthly.setTextColor(Color.parseColor("#6B7280"))
        }
    }

    private fun calcAmount(item: IncomeStreamItem): Long {
        return if (!isMonthlyMode) {
            if (item.isFixed) item.amount * 12 else item.amount
        } else {
            item.amount
        }
    }

    private fun renderIncomeUI() {
        val context = context ?: return

        val totalIncome = incomeList.sumOf { calcAmount(it) }
        val fixedIncome = incomeList.filter { it.isFixed }.sumOf { calcAmount(it) }
        val variableIncome = incomeList.filter { !it.isFixed }.sumOf { calcAmount(it) }
        val streamsCount = incomeList.size

        tvTotalIncomeHeader.text = if (isMonthlyMode) "TOTAL MONTHLY INCOME" else "TOTAL YEARLY INCOME"
        tvTotalIncome.text = "₹%,d".format(totalIncome)
        tvIncomeStreamsCount.text = "Across $streamsCount active stream${if (streamsCount != 1) "s" else ""}"
        tvFixedIncome.text = "₹%,d".format(fixedIncome)
        tvVariableIncome.text = "₹%,d".format(variableIncome)

        llIncomeTableRows.removeAllViews()

        for (item in incomeList) {
            val displayAmount = calcAmount(item)

            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                paddingStruct()
                gravity = Gravity.CENTER_VERTICAL
            }

            // 1. Income Source Name
            val tvSource = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.6f)
                text = item.source
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#111827"))
            }

            // 2. Amount
            val tvAmount = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
                text = "₹%,d".format(displayAmount)
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#111827"))
                gravity = Gravity.END
            }

            // 3. Type Pill Badge
            val typeContainer = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
                gravity = Gravity.CENTER
            }
            val (bgColor, txtColor, labelText) = if (item.isFixed) {
                Triple("#D1FAE5", "#065F46", "FIXED")
            } else {
                Triple("#F3F4F6", "#374151", "ONE-TIME")
            }

            val tvTypeBadge = TextView(context).apply {
                text = labelText
                textSize = 9f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor(txtColor))
                background = createPillDrawable(bgColor, 6f)
                setPadding(14, 6, 14, 6)
            }
            typeContainer.addView(tvTypeBadge)

            // 4. Fetch Type
            val tvFetchType = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
                text = "✏️ ${item.fetchType}"
                textSize = 11f
                setTextColor(Color.parseColor("#6B7280"))
            }

            // 5. Date of Credit
            val tvDate = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.4f)
                text = item.dateOfCredit.ifBlank { "—" }
                textSize = 11f
                typeface = Typeface.MONOSPACE
                setTextColor(Color.parseColor("#6B7280"))
            }

            // 6. Delete Button Action
            val btnDelete = ImageButton(context).apply {
                layoutParams = LinearLayout.LayoutParams(40, 40)
                setImageResource(android.R.drawable.ic_menu_delete)
                setColorFilter(Color.parseColor("#9CA3AF"))
                background = null
                setOnClickListener {
                    incomeList.remove(item)
                    renderIncomeUI()
                    Toast.makeText(context, "Income stream deleted", Toast.LENGTH_SHORT).show()
                }
            }

            row.addView(tvSource)
            row.addView(tvAmount)
            row.addView(typeContainer)
            row.addView(tvFetchType)
            row.addView(tvDate)
            row.addView(btnDelete)

            llIncomeTableRows.addView(row)

            val divider = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                setBackgroundColor(Color.parseColor("#F3F4F6"))
            }
            llIncomeTableRows.addView(divider)
        }

        // Summary Total Row at bottom
        val totalRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            paddingStruct()
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.parseColor("#F9FAFB"))
        }

        val tvTotalLabel = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.6f)
            text = if (isMonthlyMode) "Total Monthly Income" else "Total Yearly Income"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#111827"))
        }

        val tvTotalVal = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
            text = "₹%,d".format(totalIncome)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#10B981"))
            gravity = Gravity.END
        }

        val emptyFiller = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3.8f)
        }

        totalRow.addView(tvTotalLabel)
        totalRow.addView(tvTotalVal)
        totalRow.addView(emptyFiller)

        llIncomeTableRows.addView(totalRow)
    }

    private fun showAddIncomeDialog() {
        val context = context ?: return
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_income, null)

        val etIncomeSource = view.findViewById<EditText>(R.id.etIncomeSource)
        val etIncomeAmount = view.findViewById<EditText>(R.id.etIncomeAmount)
        val etDateOfCredit = view.findViewById<EditText>(R.id.etDateOfCredit)
        val rbFixed = view.findViewById<RadioButton>(R.id.rbFixed)
        val spFetchType = view.findViewById<Spinner>(R.id.spFetchType)
        val btnCancel = view.findViewById<View>(R.id.btnCancelModalIncome)
        val btnSave = view.findViewById<View>(R.id.btnSaveModalIncome)

        val fetchTypes = arrayOf("Manual", "Auto-sync")
        spFetchType.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, fetchTypes)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val source = etIncomeSource.text.toString().trim()
            val amountStr = etIncomeAmount.text.toString().trim()

            if (source.isEmpty()) {
                etIncomeSource.error = "Income source required"
                return@setOnClickListener
            }
            if (amountStr.isEmpty()) {
                etIncomeAmount.error = "Amount required"
                return@setOnClickListener
            }

            val amount = amountStr.toLongOrNull() ?: 0L
            val dateOfCredit = etDateOfCredit.text.toString().trim().ifEmpty { "2026-07-31" }
            val isFixed = rbFixed.isChecked
            val fetchType = spFetchType.selectedItem?.toString() ?: "Manual"

            val newIncome = IncomeStreamItem(
                source = source,
                amount = amount,
                isFixed = isFixed,
                fetchType = fetchType,
                dateOfCredit = dateOfCredit
            )

            incomeList.add(newIncome)
            renderIncomeUI()
            Toast.makeText(context, "Income stream added successfully!", Toast.LENGTH_SHORT).show()
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
