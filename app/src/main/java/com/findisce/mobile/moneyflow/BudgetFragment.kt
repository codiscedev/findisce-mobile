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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.findisce.mobile.R
import com.findisce.mobile.data.model.TransactionItem
import com.findisce.mobile.ui.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.UUID

data class BudgetItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val budgetAmount: Long,
    val category: String
)

class BudgetFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var tvTotalBudget: TextView
    private lateinit var tvTotalSpent: TextView
    private lateinit var tvBudgetsSet: TextView
    private lateinit var tvOverLimit: TextView
    private lateinit var llBudgetTableRows: LinearLayout

    private var allTransactions: List<TransactionItem> = emptyList()

    private val budgetList: MutableList<BudgetItem> = mutableListOf(
        BudgetItem("bud_1", "Monthly Groceries", 8000L, "Groceries"),
        BudgetItem("bud_2", "Entertainment Cap", 2000L, "Entertainment"),
        BudgetItem("bud_3", "Travel Budget", 15000L, "Travel"),
        BudgetItem("bud_4", "Shopping Limit", 10000L, "Shopping")
    )

    private val categoryColors = mapOf(
        "Groceries" to "#10B981",
        "Shopping" to "#F59E0B",
        "Food & Dining" to "#3B82F6",
        "Rent" to "#EF4444",
        "Utilities" to "#F97316",
        "Transportation" to "#06B6D4",
        "Entertainment" to "#8B5CF6",
        "Travel" to "#EC4899",
        "Miscellaneous" to "#6B7280"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget, container, false)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        tvTotalBudget = view.findViewById(R.id.tvTotalBudget)
        tvTotalSpent = view.findViewById(R.id.tvTotalSpent)
        tvBudgetsSet = view.findViewById(R.id.tvBudgetsSet)
        tvOverLimit = view.findViewById(R.id.tvOverLimit)
        llBudgetTableRows = view.findViewById(R.id.llBudgetTableRows)

        val btnAddBudget = view.findViewById<View>(R.id.btnAddBudget)
        btnAddBudget?.setOnClickListener { showAddBudgetDialog() }

        // Observe transactions list to calculate dynamic category spend
        mainViewModel.transactionsList.observe(viewLifecycleOwner) { result ->
            result.onSuccess { items ->
                allTransactions = items
                renderBudgetUI()
            }
        }

        // Fetch initial transactions
        mainViewModel.fetchTransactions()

        // Render initial state
        renderBudgetUI()

        return view
    }

    private fun renderBudgetUI() {
        val context = context ?: return

        // Calculate category spending from expenses
        val spentMap = mutableMapOf<String, Long>()
        for (item in allTransactions) {
            if (item.isExpense) {
                val cat = item.categoryName ?: "Miscellaneous"
                spentMap[cat] = (spentMap[cat] ?: 0L) + item.amount
            }
        }

        // If no transactions from API, provide fallback sample spending for demo
        if (allTransactions.isEmpty()) {
            spentMap["Groceries"] = 4500L
            spentMap["Entertainment"] = 1200L
            spentMap["Travel"] = 16200L // Over limit for travel
            spentMap["Shopping"] = 6800L
        }

        val totalBudget = budgetList.sumOf { it.budgetAmount }
        val totalSpent = budgetList.sumOf { spentMap[it.category] ?: 0L }
        val budgetsSetCount = budgetList.size
        val overLimitCount = budgetList.count { (spentMap[it.category] ?: 0L) > it.budgetAmount }

        tvTotalBudget.text = "₹%,d".format(totalBudget)
        tvTotalSpent.text = "₹%,d".format(totalSpent)
        tvBudgetsSet.text = budgetsSetCount.toString()
        tvOverLimit.text = overLimitCount.toString()

        llBudgetTableRows.removeAllViews()

        if (budgetList.isEmpty()) {
            val emptyRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                paddingStruct()
                gravity = Gravity.CENTER_VERTICAL
            }

            val tvName = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.6f)
                text = "Total Budget"
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#111827"))
            }
            val tvCat = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.4f)
                text = "—"
                textSize = 12f
                setTextColor(Color.parseColor("#9CA3AF"))
            }
            val tvBudgetVal = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
                text = "₹0"
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#2563EB"))
                gravity = Gravity.END
            }
            val tvSpentVal = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
                text = "₹0"
                textSize = 12f
                setTextColor(Color.parseColor("#374151"))
                gravity = Gravity.END
            }
            val tvRemVal = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
                text = "₹0"
                textSize = 12f
                setTextColor(Color.parseColor("#16A34A"))
                gravity = Gravity.END
            }
            val tvUtil = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.6f)
                text = "0%"
                textSize = 12f
                setTextColor(Color.parseColor("#6B7280"))
                gravity = Gravity.CENTER
            }

            emptyRow.addView(tvName)
            emptyRow.addView(tvCat)
            emptyRow.addView(tvBudgetVal)
            emptyRow.addView(tvSpentVal)
            emptyRow.addView(tvRemVal)
            emptyRow.addView(tvUtil)
            llBudgetTableRows.addView(emptyRow)
            return
        }

        for (item in budgetList) {
            val spent = spentMap[item.category] ?: 0L
            val pct = if (item.budgetAmount > 0) ((spent.toDouble() / item.budgetAmount) * 100).toInt() else 0
            val isOver = spent > item.budgetAmount
            val remaining = item.budgetAmount - spent
            val colorHex = categoryColors[item.category] ?: "#6B7280"

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

            // 2. Category with Color Dot
            val catContainer = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.4f)
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val catDot = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(16, 16).apply { marginEnd = 12 }
                background = createCircleDrawable(colorHex)
            }
            val tvCategory = TextView(context).apply {
                text = item.category
                textSize = 12f
                setTextColor(Color.parseColor("#374151"))
            }
            catContainer.addView(catDot)
            catContainer.addView(tvCategory)

            // 3. Budget Amount
            val tvBudget = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
                text = "₹%,d".format(item.budgetAmount)
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#111827"))
                gravity = Gravity.END
            }

            // 4. Spent Amount
            val tvSpent = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
                text = "₹%,d".format(spent)
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor(if (isOver) "#DC2626" else "#374151"))
                gravity = Gravity.END
            }

            // 5. Remaining Amount
            val tvRemaining = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
                text = "${if (isOver) "-₹" else "₹"}%,d".format(kotlin.math.abs(remaining))
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor(if (isOver) "#DC2626" else "#16A34A"))
                gravity = Gravity.END
            }

            // 6. Utilization Progress Bar & Percentage
            val utilContainer = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.6f).apply {
                    marginStart = 12
                    marginEnd = 12
                }
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val progressFrame = FrameLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, 16, 1f).apply { marginEnd = 8 }
                background = createRoundedRectDrawable("#F3F4F6", 8f)
            }

            val progressColorHex = when {
                isOver -> "#EF4444"
                pct > 80 -> "#F59E0B"
                else -> "#10B981"
            }

            val progressFill = View(context).apply {
                val fillPct = Math.min(pct, 100)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                background = createRoundedRectDrawable(progressColorHex, 8f)
            }
            progressFrame.addView(progressFill)

            val tvPct = TextView(context).apply {
                text = "$pct%"
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor(progressColorHex))
            }
            utilContainer.addView(progressFrame)
            utilContainer.addView(tvPct)

            // 7. Delete Button Action
            val btnDelete = ImageButton(context).apply {
                layoutParams = LinearLayout.LayoutParams(40, 40)
                setImageResource(android.R.drawable.ic_menu_delete)
                setColorFilter(Color.parseColor("#9CA3AF"))
                background = null
                setOnClickListener {
                    budgetList.remove(item)
                    renderBudgetUI()
                    Toast.makeText(context, "Budget deleted", Toast.LENGTH_SHORT).show()
                }
            }

            row.addView(tvName)
            row.addView(catContainer)
            row.addView(tvBudget)
            row.addView(tvSpent)
            row.addView(tvRemaining)
            row.addView(utilContainer)
            row.addView(btnDelete)

            llBudgetTableRows.addView(row)

            val divider = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                setBackgroundColor(Color.parseColor("#F3F4F6"))
            }
            llBudgetTableRows.addView(divider)
        }
    }

    private fun showAddBudgetDialog() {
        val context = context ?: return
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_budget, null)

        val etBudgetName = view.findViewById<EditText>(R.id.etBudgetName)
        val spBudgetCategory = view.findViewById<Spinner>(R.id.spBudgetCategory)
        val etBudgetAmount = view.findViewById<EditText>(R.id.etBudgetAmount)
        val btnCancel = view.findViewById<View>(R.id.btnCancelModalBudget)
        val btnSave = view.findViewById<View>(R.id.btnSaveModalBudget)

        val categories = arrayOf(
            "Groceries", "Shopping", "Food & Dining", "Rent",
            "Utilities", "Transportation", "Entertainment", "Travel", "Miscellaneous"
        )
        spBudgetCategory.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, categories)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val name = etBudgetName.text.toString().trim()
            val amountStr = etBudgetAmount.text.toString().trim()

            if (name.isEmpty()) {
                etBudgetName.error = "Budget name required"
                return@setOnClickListener
            }
            if (amountStr.isEmpty()) {
                etBudgetAmount.error = "Budget limit required"
                return@setOnClickListener
            }

            val amount = amountStr.toLongOrNull() ?: 0L
            val category = spBudgetCategory.selectedItem?.toString() ?: "Miscellaneous"

            val newBudget = BudgetItem(
                name = name,
                budgetAmount = amount,
                category = category
            )

            budgetList.add(newBudget)
            renderBudgetUI()
            Toast.makeText(context, "Budget saved successfully!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun createCircleDrawable(colorHex: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(colorHex))
        }
    }

    private fun createRoundedRectDrawable(colorHex: String, cornerRadiusDp: Float): GradientDrawable {
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
