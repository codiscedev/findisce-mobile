package com.findisce.mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.findisce.mobile.ui.viewmodel.MainViewModel

class HealthFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var llTxItems: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_money_flow, container, false)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        llTxItems = view.findViewById(R.id.llTxItems)
        val etTxName = view.findViewById<EditText>(R.id.etTxName)
        val etTxAmount = view.findViewById<EditText>(R.id.etTxAmount)
        val rbExpense = view.findViewById<RadioButton>(R.id.rbExpense)
        val btnAddTx = view.findViewById<Button>(R.id.btnAddTx)

        // Observe transaction item lists
        mainViewModel.transactionsList.observe(viewLifecycleOwner) { result ->
            result.onSuccess { items ->
                llTxItems.removeAllViews()
                for (item in items) {
                    addTxItemRow(item.description, item.amount, item.isExpense)
                }
            }
        }

        btnAddTx.setOnClickListener {
            val name = etTxName.text.toString().trim()
            val amountStr = etTxAmount.text.toString().trim()

            if (name.isEmpty()) {
                etTxName.error = "Description required"
                return@setOnClickListener
            }
            if (amountStr.isEmpty()) {
                etTxAmount.error = "Amount required"
                return@setOnClickListener
            }

            val amount = amountStr.toLongOrNull() ?: 0L
            val isExpense = rbExpense.isChecked

            // Trigger ViewModel additions
            mainViewModel.addTransaction(name, amount, isExpense)

            // Reset inputs
            etTxName.text.clear()
            etTxAmount.text.clear()
            Toast.makeText(context, "Transaction recorded!", Toast.LENGTH_SHORT).show()
        }

        // Fetch initial list
        mainViewModel.fetchTransactions()

        return view
    }

    private fun addTxItemRow(name: String, amount: Long, isExpense: Boolean) {
        val context = context ?: return
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(36, 28, 36, 28)
        }

        val tvName = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = name
            textSize = 13f
            setTextColor(resources.getColor(R.color.black, null))
        }

        val tvAmount = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "${if (isExpense) "-" else "+"}₹%,d".format(amount)
            textSize = 13f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(resources.getColor(if (isExpense) android.R.color.holo_red_dark else android.R.color.holo_green_dark, null))
        }

        row.addView(tvName)
        row.addView(tvAmount)
        llTxItems.addView(row, 0) // Prepend new items at the top
    }
}
