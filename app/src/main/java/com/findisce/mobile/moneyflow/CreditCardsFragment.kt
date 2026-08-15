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

data class CreditCardItem(
    val id: String = UUID.randomUUID().toString(),
    val cardName: String,
    val lastFour: String,
    val creditLimit: Long,
    val outstanding: Long,
    val minDue: Long,
    val dueDate: String
)

class CreditCardsFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var tvTotalOutstanding: TextView
    private lateinit var tvTotalLimit: TextView
    private lateinit var tvOverallUtilization: TextView
    private lateinit var tvActiveCards: TextView
    private lateinit var llCreditCardRows: LinearLayout

    private val cardsList: MutableList<CreditCardItem> = mutableListOf(
        CreditCardItem("cc_1", "HDFC Regalia", "4829", 300000L, 24500L, 1225L, "2026-07-20"),
        CreditCardItem("cc_2", "SBI SimplyCLICK", "9930", 100000L, 11200L, 560L, "2026-07-10")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_credit_cards, container, false)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        tvTotalOutstanding = view.findViewById(R.id.tvTotalOutstanding)
        tvTotalLimit = view.findViewById(R.id.tvTotalLimit)
        tvOverallUtilization = view.findViewById(R.id.tvOverallUtilization)
        tvActiveCards = view.findViewById(R.id.tvActiveCards)
        llCreditCardRows = view.findViewById(R.id.llCreditCardRows)

        val btnAddCard = view.findViewById<View>(R.id.btnAddCard)
        btnAddCard?.setOnClickListener { showAddCardDialog() }

        renderCreditCardsUI()

        return view
    }

    private fun renderCreditCardsUI() {
        val context = context ?: return

        val totalOutstanding = cardsList.sumOf { it.outstanding }
        val totalLimit = cardsList.sumOf { it.creditLimit }
        val overallUtilization = if (totalLimit > 0) Math.round((totalOutstanding.toDouble() / totalLimit) * 100).toInt() else 0
        val activeCount = cardsList.size

        tvTotalOutstanding.text = "₹%,d".format(totalOutstanding)
        tvTotalLimit.text = "₹%,d".format(totalLimit)
        tvOverallUtilization.text = "$overallUtilization%"
        tvActiveCards.text = activeCount.toString()

        // Color overall utilization based on percentage
        if (overallUtilization > 60) {
            tvOverallUtilization.setTextColor(Color.parseColor("#DC2626"))
        } else if (overallUtilization > 30) {
            tvOverallUtilization.setTextColor(Color.parseColor("#D97706"))
        } else {
            tvOverallUtilization.setTextColor(Color.parseColor("#10B981"))
        }

        llCreditCardRows.removeAllViews()

        for (item in cardsList) {
            val cardUtilization = if (item.creditLimit > 0) Math.round((item.outstanding.toDouble() / item.creditLimit) * 100).toInt() else 0

            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                paddingStruct()
                gravity = Gravity.CENTER_VERTICAL
            }

            // 1. Card Name
            val tvName = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.6f)
                text = item.cardName
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#111827"))
            }

            // 2. Last 4 Digits
            val tvLastFour = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
                text = if (item.lastFour.isNotBlank()) "•••• ${item.lastFour}" else "—"
                textSize = 11f
                typeface = Typeface.MONOSPACE
                setTextColor(Color.parseColor("#6B7280"))
            }

            // 3. Credit Limit
            val tvLimit = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.3f)
                text = "₹%,d".format(item.creditLimit)
                textSize = 12f
                setTextColor(Color.parseColor("#374151"))
                gravity = Gravity.END
            }

            // 4. Outstanding Amount
            val tvOutstanding = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.3f)
                text = "₹%,d".format(item.outstanding)
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(if (item.outstanding > 0) Color.parseColor("#DC2626") else Color.parseColor("#111827"))
                gravity = Gravity.END
            }

            // 5. Min Due
            val tvMinDue = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.1f)
                text = "₹%,d".format(item.minDue)
                textSize = 12f
                setTextColor(Color.parseColor("#4B5563"))
                gravity = Gravity.END
            }

            // 6. Due Date
            val tvDueDate = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
                text = item.dueDate.ifBlank { "—" }
                textSize = 11f
                typeface = Typeface.MONOSPACE
                setTextColor(Color.parseColor("#6B7280"))
                gravity = Gravity.CENTER
            }

            // 7. Utilization Pill Badge
            val utilContainer = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
                gravity = Gravity.CENTER
            }

            val (bgColor, txtColor) = when {
                cardUtilization > 60 -> Pair("#FEE2E2", "#DC2626")
                cardUtilization > 30 -> Pair("#FEF3C7", "#D97706")
                else -> Pair("#D1FAE5", "#059669")
            }

            val tvUtilBadge = TextView(context).apply {
                text = "$cardUtilization%"
                textSize = 9f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor(txtColor))
                background = createPillDrawable(bgColor, 6f)
                setPadding(12, 4, 12, 4)
            }
            utilContainer.addView(tvUtilBadge)

            // 8. Delete Button Action
            val btnDelete = ImageButton(context).apply {
                layoutParams = LinearLayout.LayoutParams(40, 40)
                setImageResource(android.R.drawable.ic_menu_delete)
                setColorFilter(Color.parseColor("#9CA3AF"))
                background = null
                setOnClickListener {
                    cardsList.remove(item)
                    renderCreditCardsUI()
                    Toast.makeText(context, "Card deleted", Toast.LENGTH_SHORT).show()
                }
            }

            row.addView(tvName)
            row.addView(tvLastFour)
            row.addView(tvLimit)
            row.addView(tvOutstanding)
            row.addView(tvMinDue)
            row.addView(tvDueDate)
            row.addView(utilContainer)
            row.addView(btnDelete)

            llCreditCardRows.addView(row)

            val divider = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                setBackgroundColor(Color.parseColor("#F3F4F6"))
            }
            llCreditCardRows.addView(divider)
        }

        // Summary Total Row at bottom
        val totalRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            paddingStruct()
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.parseColor("#F9FAFB"))
        }

        val tvTotalLabel = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3.9f)
            text = "Total Outstanding"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#111827"))
        }

        val tvTotalVal = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.3f)
            text = "₹%,d".format(totalOutstanding)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#DC2626"))
            gravity = Gravity.END
        }

        val emptyFiller = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3.5f)
        }

        totalRow.addView(tvTotalLabel)
        totalRow.addView(tvTotalVal)
        totalRow.addView(emptyFiller)

        llCreditCardRows.addView(totalRow)
    }

    private fun showAddCardDialog() {
        val context = context ?: return
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_credit_card, null)

        val etCardName = view.findViewById<EditText>(R.id.etCardName)
        val etLastFour = view.findViewById<EditText>(R.id.etLastFour)
        val etCreditLimit = view.findViewById<EditText>(R.id.etCreditLimit)
        val etOutstanding = view.findViewById<EditText>(R.id.etOutstanding)
        val etMinDue = view.findViewById<EditText>(R.id.etMinDue)
        val etDueDate = view.findViewById<EditText>(R.id.etDueDate)
        val btnCancel = view.findViewById<View>(R.id.btnCancelModalCard)
        val btnSave = view.findViewById<View>(R.id.btnSaveModalCard)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val name = etCardName.text.toString().trim()
            val limitStr = etCreditLimit.text.toString().trim()

            if (name.isEmpty()) {
                etCardName.error = "Card name required"
                return@setOnClickListener
            }
            if (limitStr.isEmpty()) {
                etCreditLimit.error = "Credit limit required"
                return@setOnClickListener
            }

            val limit = limitStr.toLongOrNull() ?: 0L
            val lastFour = etLastFour.text.toString().trim()
            val outstanding = etOutstanding.text.toString().trim().toLongOrNull() ?: 0L
            val minDue = etMinDue.text.toString().trim().toLongOrNull() ?: 0L
            val dueDate = etDueDate.text.toString().trim().ifEmpty { "2026-07-20" }

            val newCard = CreditCardItem(
                cardName = name,
                lastFour = lastFour,
                creditLimit = limit,
                outstanding = outstanding,
                minDue = minDue,
                dueDate = dueDate
            )

            cardsList.add(newCard)
            renderCreditCardsUI()
            Toast.makeText(context, "Credit card added successfully!", Toast.LENGTH_SHORT).show()
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
