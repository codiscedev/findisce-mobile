package com.findisce.mobile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.findisce.mobile.ui.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class WealthFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var tvAssetsTotal: TextView
    private lateinit var tvDebtsTotal: TextView
    private lateinit var tvInvestmentsTotal: TextView
    private lateinit var tvAppreciatingAssets: TextView
    private lateinit var tvDepreciatingAssets: TextView
    private lateinit var tvActiveLiabilities: TextView
    private lateinit var tvTotalEmi: TextView
    private lateinit var tvAssetsConnectedCount: TextView
    private lateinit var llAssetsList: LinearLayout
    private lateinit var llDebtsList: LinearLayout
    private lateinit var tvNoAssetsPlaceholder: TextView
    private lateinit var tvNoDebtsPlaceholder: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_wealth, container, false)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        tvAssetsTotal = view.findViewById(R.id.tvAssetsTotal)
        tvDebtsTotal = view.findViewById(R.id.tvDebtsTotal)
        tvInvestmentsTotal = view.findViewById(R.id.tvInvestmentsTotal)
        tvAppreciatingAssets = view.findViewById(R.id.tvAppreciatingAssets)
        tvDepreciatingAssets = view.findViewById(R.id.tvDepreciatingAssets)
        tvActiveLiabilities = view.findViewById(R.id.tvActiveLiabilities)
        tvTotalEmi = view.findViewById(R.id.tvTotalEmi)
        tvAssetsConnectedCount = view.findViewById(R.id.tvAssetsConnectedCount)

        llAssetsList = view.findViewById(R.id.llAssetsList)
        llDebtsList = view.findViewById(R.id.llDebtsList)
        tvNoAssetsPlaceholder = view.findViewById(R.id.tvNoAssetsPlaceholder)
        tvNoDebtsPlaceholder = view.findViewById(R.id.tvNoDebtsPlaceholder)

        val btnAddWealthTop = view.findViewById<View>(R.id.btnAddWealthTop)
        val btnAssetDetails = view.findViewById<View>(R.id.btnAssetDetails)

        // Setup Add Action Listeners
        btnAddWealthTop?.setOnClickListener { showRecordTypeSelectorSheet() }
        btnAssetDetails?.setOnClickListener {
            val intent = Intent(context, AssetDetailsActivity::class.java)
            startActivity(intent)
        }

        // Observe wealth items
        mainViewModel.wealthItems.observe(viewLifecycleOwner) { result ->
            result.onSuccess { items ->
                llAssetsList.removeAllViews()
                llDebtsList.removeAllViews()

                var assetsTotal = 0L
                var debtsTotal = 0L
                var assetCount = 0
                var debtCount = 0

                for (item in items) {
                    if (item.isAsset) {
                        assetsTotal += item.amount
                        assetCount++
                        addWealthRow(llAssetsList, item.name, item.amount, true)
                    } else {
                        debtsTotal += item.amount
                        debtCount++
                        addWealthRow(llDebtsList, item.name, item.amount, false)
                    }
                }

                // Toggle placeholders
                tvNoAssetsPlaceholder.visibility = if (assetCount == 0) View.VISIBLE else View.GONE
                if (assetCount == 0) llAssetsList.addView(tvNoAssetsPlaceholder)

                tvNoDebtsPlaceholder.visibility = if (debtCount == 0) View.VISIBLE else View.GONE
                if (debtCount == 0) llDebtsList.addView(tvNoDebtsPlaceholder)

                // Update summary texts
                tvAssetsTotal.text = "₹%,d".format(assetsTotal)
                tvDebtsTotal.text = "₹%,d".format(debtsTotal)
                tvInvestmentsTotal.text = "₹%,d".format((assetsTotal * 0.4).toLong())
                tvAppreciatingAssets.text = "₹%,d".format((assetsTotal * 0.7).toLong())
                tvDepreciatingAssets.text = "₹%,d".format((assetsTotal * 0.3).toLong())
                tvAssetsConnectedCount.text = "across $assetCount connected assets"
                tvActiveLiabilities.text = if (debtCount > 0) "$debtCount active liabilities" else "No active liabilities"
                tvTotalEmi.text = "₹%,d / mo".format((debtsTotal * 0.05).toLong())
            }
        }

        // Fetch initial list and asset categories from API
        mainViewModel.fetchWealthItems()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Refresh wealth items when returning from ChooseCategoryActivity
        mainViewModel.fetchWealthItems()
    }

    private fun showRecordTypeSelectorSheet() {
        val context = context ?: return
        val dialog = BottomSheetDialog(context)
        val sheetView = LayoutInflater.from(context).inflate(R.layout.dialog_add_wealth_flow, null)

        val cardTypeAsset = sheetView.findViewById<View>(R.id.cardTypeAsset)
        val cardTypeDebt = sheetView.findViewById<View>(R.id.cardTypeDebt)
        val cardTypeInvestment = sheetView.findViewById<View>(R.id.cardTypeInvestment)
        val cardTypeGoal = sheetView.findViewById<View>(R.id.cardTypeGoal)
        val cardTypeEssential = sheetView.findViewById<View>(R.id.cardTypeEssential)

        cardTypeAsset.setOnClickListener {
            dialog.dismiss()
            openChooseCategory("asset")
        }
        cardTypeDebt.setOnClickListener {
            dialog.dismiss()
            openChooseCategory("debt")
        }
        cardTypeInvestment.setOnClickListener {
            dialog.dismiss()
            openChooseCategory("investment")
        }
        cardTypeGoal.setOnClickListener {
            dialog.dismiss()
            openChooseCategory("goal")
        }
        cardTypeEssential.setOnClickListener {
            dialog.dismiss()
            openChooseCategory("essential")
        }

        dialog.setContentView(sheetView)
        dialog.show()
    }

    private fun openChooseCategory(recordType: String) {
        val intent = Intent(context, ChooseCategoryActivity::class.java).apply {
            putExtra("RECORD_TYPE", recordType)
        }
        startActivity(intent)
    }

    private fun addWealthRow(parent: LinearLayout, name: String, amount: Long, isAsset: Boolean) {
        val context = context ?: return
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(24, 16, 24, 16)
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
        parent.addView(row)
    }
}
