package com.findisce.mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.findisce.mobile.data.model.AssetCategoryResponseItem
import com.findisce.mobile.ui.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton

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
        val btnAddAsset = view.findViewById<View>(R.id.btnAddAsset)
        val btnAddLoan = view.findViewById<View>(R.id.btnAddLoan)
        val btnAddGoal = view.findViewById<View>(R.id.btnAddGoal)
        val btnAddEssential = view.findViewById<View>(R.id.btnAddEssential)
        val btnAddInvestment = view.findViewById<View>(R.id.btnAddInvestment)

        // Setup Add Action Listeners
        btnAddWealthTop?.setOnClickListener { showRecordTypeSelectorSheet() }
        btnAddAsset?.setOnClickListener { showAssetCategorySelectorSheet() }
        btnAddLoan?.setOnClickListener { showCategorySelectorSheet("Debt / Liability") }
        btnAddGoal?.setOnClickListener { showCategorySelectorSheet("Financial Goal") }
        btnAddEssential?.setOnClickListener { showCategorySelectorSheet("Essentials") }
        btnAddInvestment?.setOnClickListener { showCategorySelectorSheet("Investment") }

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
        mainViewModel.fetchAssetCategories()

        return view
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
            showAssetCategorySelectorSheet()
        }
        cardTypeDebt.setOnClickListener {
            dialog.dismiss()
            showCategorySelectorSheet("Debt / Liability")
        }
        cardTypeInvestment.setOnClickListener {
            dialog.dismiss()
            showCategorySelectorSheet("Investment")
        }
        cardTypeGoal.setOnClickListener {
            dialog.dismiss()
            showCategorySelectorSheet("Financial Goal")
        }
        cardTypeEssential.setOnClickListener {
            dialog.dismiss()
            showCategorySelectorSheet("Essentials")
        }

        dialog.setContentView(sheetView)
        dialog.show()
    }

    private fun showAssetCategorySelectorSheet() {
        val context = context ?: return
        val dialog = BottomSheetDialog(context)
        val sheetView = LayoutInflater.from(context).inflate(R.layout.dialog_select_asset_category, null)

        val btnBackToType = sheetView.findViewById<View>(R.id.btnBackToType)
        val gridAssetCategories = sheetView.findViewById<GridLayout>(R.id.gridAssetCategories)

        btnBackToType.setOnClickListener {
            dialog.dismiss()
            showRecordTypeSelectorSheet()
        }

        // Trigger API call for asset categories
        mainViewModel.fetchAssetCategories()

        fun populateCategoryGrid(categories: List<AssetCategoryResponseItem>) {
            gridAssetCategories.removeAllViews()
            for (categoryItem in categories) {
                val catName = categoryItem.name ?: continue
                val categoryBtn = MaterialButton(context).apply {
                    text = catName
                    isAllCaps = false
                    setTextColor(resources.getColor(R.color.black, null))
                    strokeColor = android.content.res.ColorStateList.valueOf(resources.getColor(android.R.color.darker_gray, null))
                    strokeWidth = 2
                    cornerRadius = 24
                    setOnClickListener {
                        dialog.dismiss()
                        showAssetFormSheet(
                            categoryName = catName,
                            isAppreciation = categoryItem.isAppreciation ?: true,
                            defaultRate = categoryItem.rate?.toDouble() ?: 8.0
                        )
                    }
                }
                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 120
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
                gridAssetCategories.addView(categoryBtn, params)
            }
        }

        val cachedCategories = mainViewModel.assetCategories.value?.getOrNull()
        if (!cachedCategories.isNullOrEmpty()) {
            populateCategoryGrid(cachedCategories)
        } else {
            // Fallback list while API request is in-flight
            val fallbackCategories = listOf(
                AssetCategoryResponseItem(null, null, "Property", true, java.math.BigDecimal(8)),
                AssetCategoryResponseItem(null, null, "Gold", true, java.math.BigDecimal(5)),
                AssetCategoryResponseItem(null, null, "Silver", true, java.math.BigDecimal(4)),
                AssetCategoryResponseItem(null, null, "Vehicle", false, java.math.BigDecimal(10)),
                AssetCategoryResponseItem(null, null, "Liquid Cash", true, java.math.BigDecimal(0)),
                AssetCategoryResponseItem(null, null, "Savings Bank Account", true, java.math.BigDecimal(4)),
                AssetCategoryResponseItem(null, null, "Others", true, java.math.BigDecimal(8))
            )
            populateCategoryGrid(fallbackCategories)
        }

        mainViewModel.assetCategories.observe(viewLifecycleOwner) { result ->
            result.onSuccess { apiCategories ->
                if (apiCategories.isNotEmpty() && dialog.isShowing) {
                    populateCategoryGrid(apiCategories)
                }
            }
        }

        dialog.setContentView(sheetView)
        dialog.show()
    }

    private fun showCategorySelectorSheet(categoryType: String) {
        if (categoryType == "Asset") {
            showAssetCategorySelectorSheet()
            return
        }

        val context = context ?: return
        val dialog = BottomSheetDialog(context)
        val sheetView = LayoutInflater.from(context).inflate(R.layout.dialog_select_asset_category, null)

        val btnBackToType = sheetView.findViewById<View>(R.id.btnBackToType)
        val gridAssetCategories = sheetView.findViewById<GridLayout>(R.id.gridAssetCategories)

        btnBackToType.setOnClickListener {
            dialog.dismiss()
            showRecordTypeSelectorSheet()
        }

        fun populateCategoryGrid(categories: List<String>) {
            gridAssetCategories.removeAllViews()
            for (catName in categories) {
                val categoryBtn = MaterialButton(context).apply {
                    text = catName
                    isAllCaps = false
                    setTextColor(resources.getColor(R.color.black, null))
                    strokeColor = android.content.res.ColorStateList.valueOf(resources.getColor(android.R.color.darker_gray, null))
                    strokeWidth = 2
                    cornerRadius = 24
                    setOnClickListener {
                        dialog.dismiss()
                        showRecordFormDialog(categoryType, catName)
                    }
                }
                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 120
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
                gridAssetCategories.addView(categoryBtn, params)
            }
        }

        when (categoryType) {
            "Debt / Liability" -> {
                mainViewModel.fetchDebtCategories()
                mainViewModel.debtCategories.observe(viewLifecycleOwner) { res ->
                    res.onSuccess { list ->
                        val names = list.mapNotNull { it.name }
                        if (names.isNotEmpty() && dialog.isShowing) populateCategoryGrid(names)
                    }
                }
                val defaultDebts = listOf("Home Loan", "Personal Loan", "Car Loan", "Credit Card", "Education Loan", "Others")
                populateCategoryGrid(defaultDebts)
            }
            "Investment" -> {
                mainViewModel.fetchInvestmentCategories()
                mainViewModel.investmentCategories.observe(viewLifecycleOwner) { res ->
                    res.onSuccess { list ->
                        val names = list.mapNotNull { it.name }
                        if (names.isNotEmpty() && dialog.isShowing) populateCategoryGrid(names)
                    }
                }
                val defaultInvestments = listOf("Mutual Funds", "Stocks / Equities", "Fixed Deposits (FD)", "Gold Commodity SIP", "Bonds", "Crypto", "Others")
                populateCategoryGrid(defaultInvestments)
            }
            "Financial Goal" -> {
                mainViewModel.fetchGoalCategories()
                mainViewModel.goalCategories.observe(viewLifecycleOwner) { res ->
                    res.onSuccess { list ->
                        val names = list.mapNotNull { it.name }
                        if (names.isNotEmpty() && dialog.isShowing) populateCategoryGrid(names)
                    }
                }
                val defaultGoals = listOf("Retirement Fund", "Emergency Buffer", "Child Education", "Home Purchase", "Family Vacation", "Others")
                populateCategoryGrid(defaultGoals)
            }
            "Essentials" -> {
                mainViewModel.fetchEssentialCategories()
                mainViewModel.essentialCategories.observe(viewLifecycleOwner) { res ->
                    res.onSuccess { list ->
                        val names = list.mapNotNull { it.name }
                        if (names.isNotEmpty() && dialog.isShowing) populateCategoryGrid(names)
                    }
                }
                val defaultEssentials = listOf("Term Life Insurance", "Health Insurance", "Vehicle Insurance", "Will & Estate Plan", "Safety Reserve", "Others")
                populateCategoryGrid(defaultEssentials)
            }
        }

        dialog.setContentView(sheetView)
        dialog.show()
    }

    private fun showAssetFormSheet(categoryName: String, isAppreciation: Boolean = true, defaultRate: Double = 8.0) {
        val context = context ?: return
        val dialog = BottomSheetDialog(context)
        val formView = LayoutInflater.from(context).inflate(R.layout.dialog_asset_form, null)

        val btnBackToCategory = formView.findViewById<View>(R.id.btnBackToCategory)
        val etSelectedCategory = formView.findViewById<EditText>(R.id.etSelectedCategory)
        val spAssetType = formView.findViewById<Spinner>(R.id.spAssetType)
        val etAppreciationRate = formView.findViewById<EditText>(R.id.etAppreciationRate)
        val lblAssetName = formView.findViewById<TextView>(R.id.lblAssetName)
        val etAssetName = formView.findViewById<EditText>(R.id.etAssetName)
        val lblPropertyType = formView.findViewById<TextView>(R.id.lblPropertyType)
        val spPropertyType = formView.findViewById<Spinner>(R.id.spPropertyType)
        val etPurchaseValue = formView.findViewById<EditText>(R.id.etPurchaseValue)
        val etPurchaseDate = formView.findViewById<EditText>(R.id.etPurchaseDate)
        val etCurrentMarketValue = formView.findViewById<EditText>(R.id.etCurrentMarketValue)
        val etNote = formView.findViewById<EditText>(R.id.etNote)
        val btnCancel = formView.findViewById<View>(R.id.btnCancelAssetForm)
        val btnSave = formView.findViewById<View>(R.id.btnSaveAssetForm)

        // Pre-fill Category & Rates returned from API
        etSelectedCategory.setText(categoryName)
        etAppreciationRate.setText(defaultRate.toString())

        val assetTypeAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, arrayOf("APPRECIATION", "DEPRECIATION"))
        spAssetType.adapter = assetTypeAdapter
        spAssetType.setSelection(if (isAppreciation) 0 else 1)

        // Customise label & subtype options based on category name
        when (categoryName.uppercase()) {
            "PROPERTY" -> {
                lblAssetName.text = "Property Name *"
                etAssetName.hint = "E.g. Sea-face 2BHK flat"
                lblPropertyType.text = "Property Type *"
                lblPropertyType.visibility = View.VISIBLE
                spPropertyType.visibility = View.VISIBLE
                spPropertyType.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, arrayOf("Apartment", "Villa", "Plot", "Commercial"))
            }
            "VEHICLE" -> {
                lblAssetName.text = "Vehicle Name *"
                etAssetName.hint = "E.g. Honda City 2024"
                lblPropertyType.text = "Vehicle Type *"
                lblPropertyType.visibility = View.VISIBLE
                spPropertyType.visibility = View.VISIBLE
                spPropertyType.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, arrayOf("Car", "Bike", "Electric Vehicle", "Commercial"))
            }
            "SAVINGS BANK ACCOUNT" -> {
                lblAssetName.text = "Account / Bank Name *"
                etAssetName.hint = "E.g. HDFC Salary Account"
                lblPropertyType.visibility = View.GONE
                spPropertyType.visibility = View.GONE
            }
            else -> {
                lblAssetName.text = "Asset Name *"
                etAssetName.hint = "E.g. $categoryName Item"
                lblPropertyType.visibility = View.GONE
                spPropertyType.visibility = View.GONE
            }
        }

        btnBackToCategory.setOnClickListener {
            dialog.dismiss()
            showAssetCategorySelectorSheet()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val assetName = etAssetName.text.toString().trim()
            val purchaseValStr = etPurchaseValue.text.toString().trim()
            val currentMarketValStr = etCurrentMarketValue.text.toString().trim()

            if (assetName.isEmpty()) {
                etAssetName.error = "Asset name is required"
                return@setOnClickListener
            }
            if (purchaseValStr.isEmpty()) {
                etPurchaseValue.error = "Purchase value is required"
                return@setOnClickListener
            }

            val purchaseVal = purchaseValStr.toLongOrNull() ?: 0L
            val currentMarketVal = currentMarketValStr.toLongOrNull() ?: purchaseVal
            val assetType = spAssetType.selectedItem?.toString() ?: "APPRECIATION"
            val rate = etAppreciationRate.text.toString().toDoubleOrNull() ?: defaultRate
            val propType = if (spPropertyType.visibility == View.VISIBLE) spPropertyType.selectedItem?.toString() else null
            val note = etNote.text.toString().trim().ifEmpty { null }

            mainViewModel.createAssetDetailed(
                categoryName = categoryName,
                assetName = assetName,
                assetType = assetType,
                appreciationRate = rate,
                purchaseValue = purchaseVal,
                currentMarketValue = currentMarketVal,
                propertyType = propType,
                note = note
            )

            Toast.makeText(context, "Asset record created successfully!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.setContentView(formView)
        dialog.show()
    }

    private fun showRecordFormDialog(categoryType: String, subCategoryName: String? = null) {
        val context = context ?: return
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_wealth, null)
        val etName = dialogView.findViewById<EditText>(R.id.etDialogWealthName)
        val etAmount = dialogView.findViewById<EditText>(R.id.etDialogWealthAmount)
        val rgType = dialogView.findViewById<RadioGroup>(R.id.rgDialogWealthType)
        rgType.visibility = View.GONE

        val defaultHint = subCategoryName ?: categoryType
        etName.hint = "Name (e.g. $defaultHint)"

        AlertDialog.Builder(context)
            .setTitle("Add $categoryType (${subCategoryName ?: "General"})")
            .setView(dialogView)
            .setPositiveButton("Save Record") { _, _ ->
                val name = etName.text.toString().trim()
                val amountStr = etAmount.text.toString().trim()

                if (name.isEmpty() || amountStr.isEmpty()) {
                    Toast.makeText(context, "Please enter name and amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val amount = amountStr.toLongOrNull() ?: 0L

                when (categoryType) {
                    "Debt / Liability" -> mainViewModel.createDebt(name, amount)
                    "Investment" -> mainViewModel.createInvestment(name, amount)
                    "Financial Goal" -> mainViewModel.createGoal(name, amount)
                    "Essentials" -> mainViewModel.createEssential(name, amount)
                    else -> mainViewModel.createAsset(name, amount)
                }

                Toast.makeText(context, "$categoryType record created!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
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
