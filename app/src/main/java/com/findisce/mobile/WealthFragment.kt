package com.findisce.mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.findisce.mobile.ui.viewmodel.MainViewModel

class WealthFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var tvTotalAssets: TextView
    private lateinit var tvTotalDebts: TextView
    private lateinit var llWealthItems: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_wealth, container, false)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        tvTotalAssets = view.findViewById(R.id.tvTotalAssets)
        tvTotalDebts = view.findViewById(R.id.tvTotalDebts)
        llWealthItems = view.findViewById(R.id.llWealthItems)

        val etWealthName = view.findViewById<EditText>(R.id.etWealthName)
        val etWealthAmount = view.findViewById<EditText>(R.id.etWealthAmount)
        val rbAsset = view.findViewById<RadioButton>(R.id.rbAsset)
        val btnAddWealth = view.findViewById<Button>(R.id.btnAddWealth)

        // Observe wealth item lists
        mainViewModel.wealthItems.observe(viewLifecycleOwner) { result ->
            result.onSuccess { items ->
                llWealthItems.removeAllViews()
                var assetsTotal = 0L
                var debtsTotal = 0L

                for (item in items) {
                    if (item.isAsset) {
                        assetsTotal += item.amount
                    } else {
                        debtsTotal += item.amount
                    }
                    addWealthItemRow(item.name, item.amount, item.isAsset)
                }

                tvTotalAssets.text = "₹%,d".format(assetsTotal)
                tvTotalDebts.text = "₹%,d".format(debtsTotal)
            }
        }

        btnAddWealth.setOnClickListener {
            val name = etWealthName.text.toString().trim()
            val amountStr = etWealthAmount.text.toString().trim()

            if (name.isEmpty()) {
                etWealthName.error = "Name required"
                return@setOnClickListener
            }
            if (amountStr.isEmpty()) {
                etWealthAmount.error = "Amount required"
                return@setOnClickListener
            }

            val amount = amountStr.toLongOrNull() ?: 0L
            val isAsset = rbAsset.isChecked

            // Trigger ViewModel additions
            mainViewModel.addWealthItem(name, amount, isAsset)

            // Reset inputs
            etWealthName.text.clear()
            etWealthAmount.text.clear()
            Toast.makeText(context, "Wealth item added!", Toast.LENGTH_SHORT).show()
        }

        // Fetch initial list
        mainViewModel.fetchWealthItems()

        return view
    }

    private fun addWealthItemRow(name: String, amount: Long, isAsset: Boolean) {
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
            text = "${if (isAsset) "+" else "-"}₹%,d".format(amount)
            textSize = 13f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(resources.getColor(if (isAsset) android.R.color.holo_green_dark else android.R.color.holo_red_dark, null))
        }

        row.addView(tvName)
        row.addView(tvAmount)
        llWealthItems.addView(row)
    }
}
