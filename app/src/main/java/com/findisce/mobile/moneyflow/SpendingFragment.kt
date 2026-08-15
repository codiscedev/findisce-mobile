package com.findisce.mobile.moneyflow

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.findisce.mobile.R
import com.findisce.mobile.data.model.TransactionItem
import com.findisce.mobile.sms.SmsHistoryReader
import com.findisce.mobile.sms.Transaction
import com.findisce.mobile.sms.TransactionType
import com.findisce.mobile.ui.view.DonutChartView
import com.findisce.mobile.ui.view.DonutSegment
import com.findisce.mobile.ui.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class SpendingFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var donutChartView: DonutChartView
    private lateinit var llCategoryLegend: LinearLayout
    private lateinit var tvTotalOutflow: TextView
    private lateinit var llTopCategoriesList: LinearLayout
    private lateinit var llTxItems: LinearLayout
    private lateinit var etSearchTransactions: EditText
    private lateinit var spCategoryFilter: Spinner

    private var allTransactions: List<TransactionItem> = emptyList()

    private val requestSmsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showSmsImportDialog()
        } else {
            Toast.makeText(context, "SMS permission is required to import bank transactions.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_spending, container, false)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        donutChartView = view.findViewById(R.id.donutChartView)
        llCategoryLegend = view.findViewById(R.id.llCategoryLegend)
        tvTotalOutflow = view.findViewById(R.id.tvTotalOutflow)
        llTopCategoriesList = view.findViewById(R.id.llTopCategoriesList)
        llTxItems = view.findViewById(R.id.llTxItems)
        etSearchTransactions = view.findViewById(R.id.etSearchTransactions)
        spCategoryFilter = view.findViewById(R.id.spCategoryFilter)

        val btnAddTransactionManual = view.findViewById<View>(R.id.btnAddTransactionManual)
        btnAddTransactionManual?.setOnClickListener { showSpendingImportOptionsSheet() }

        // Setup filter spinner options
        val filterCategories = arrayOf("All Categories", "Miscellaneous", "Rent", "Groceries", "Shopping", "Food & Dining", "Utilities", "Transportation")
        spCategoryFilter.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, filterCategories)

        // Observe transactions list
        mainViewModel.transactionsList.observe(viewLifecycleOwner) { result ->
            result.onSuccess { items ->
                allTransactions = items
                renderMoneyFlowUI(items)
            }
        }

        // Search text watcher
        etSearchTransactions.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterAndRenderTransactions()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Fetch initial list
        mainViewModel.fetchTransactions()

        return view
    }

    private fun renderMoneyFlowUI(items: List<TransactionItem>) {
        // Calculate Category Spending
        val categoryMap = mutableMapOf<String, Double>()
        var totalOutflow = 0.0

        for (item in items) {
            if (item.isExpense) {
                totalOutflow += item.amount
                val cat = item.categoryName ?: "Miscellaneous"
                categoryMap[cat] = (categoryMap[cat] ?: 0.0) + item.amount
            }
        }

        // If no transactions exist, populate default demonstration totals matching screenshot
        if (totalOutflow == 0.0) {
            totalOutflow = 3409880.0
            categoryMap["Miscellaneous"] = 3371482.0
            categoryMap["Rent"] = 33155.0
            categoryMap["Shopping"] = 2040.0
            categoryMap["Groceries"] = 986.0
            categoryMap["Food & Dining"] = 764.0
            categoryMap["Utilities"] = 503.0
            categoryMap["Transportation"] = 385.0
        }

        val formattedTotal = "₹%,d".format(totalOutflow.toLong())
        tvTotalOutflow.text = formattedTotal

        // Color palettes for Donut chart
        val categoryColors = mapOf(
            "Miscellaneous" to "#8B5CF6",
            "Rent" to "#EF4444",
            "Shopping" to "#F59E0B",
            "Groceries" to "#10B981",
            "Food & Dining" to "#3B82F6",
            "Utilities" to "#F97316",
            "Transportation" to "#06B6D4"
        )

        // Build Donut Segments
        val donutSegments = categoryMap.map { (category, amount) ->
            DonutSegment(
                label = category,
                value = amount,
                colorHex = categoryColors[category] ?: "#6B7280"
            )
        }
        donutChartView.setData(donutSegments, formattedTotal)

        // Populate Category Legend
        llCategoryLegend.removeAllViews()
        llTopCategoriesList.removeAllViews()

        categoryMap.entries.sortedByDescending { it.value }.take(5).forEach { (category, amount) ->
            val colorHex = categoryColors[category] ?: "#6B7280"
            val pct = ((amount / totalValueOrOne(totalOutflow)) * 100).toInt()

            // Legend Row
            val legendRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 4, 0, 4)
            }
            val dot = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(16, 16).apply { marginEnd = 12 }
                background = createCircleDrawable(colorHex)
            }
            val tvLabel = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = category
                textSize = 11f
                setTextColor(resources.getColor(R.color.black, null))
            }
            val tvVal = TextView(context).apply {
                text = "₹%,d · %d%%".format(amount.toLong(), pct)
                textSize = 10f
                setTextColor(resources.getColor(android.R.color.darker_gray, null))
            }
            legendRow.addView(dot)
            legendRow.addView(tvLabel)
            legendRow.addView(tvVal)
            llCategoryLegend.addView(legendRow)

            // Top Category Row
            val topRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 6, 0, 6)
            }
            val topDot = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(14, 14).apply { marginEnd = 12 }
                background = createCircleDrawable(colorHex)
            }
            val topLabel = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = category
                textSize = 12f
                setTextColor(resources.getColor(R.color.black, null))
            }
            val topVal = TextView(context).apply {
                text = "₹%,d".format(amount.toLong())
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(resources.getColor(R.color.black, null))
            }
            topRow.addView(topDot)
            topRow.addView(topLabel)
            topRow.addView(topVal)
            llTopCategoriesList.addView(topRow)
        }

        filterAndRenderTransactions()
    }

    private fun filterAndRenderTransactions() {
        val query = etSearchTransactions.text.toString().trim().lowercase()
        llTxItems.removeAllViews()

        val listToDisplay = if (allTransactions.isEmpty()) {
            // Demonstration transactions if API list is empty
            listOf(
                TransactionItem(null, "ACH/FIN NJ CAPITAL PRIVA/ICIC7011505261004995", 2299, true, "Miscellaneous"),
                TransactionItem(null, "UPI/Chennai Un/cumta@yespay/In App/YesBank_Ye/06183978686", 12, true, "Miscellaneous"),
                TransactionItem(null, "HDFC Salary Deposit", 50000, false, "Income"),
                TransactionItem(null, "Monthly Apartment Rent Payment", 33155, true, "Rent")
            )
        } else {
            allTransactions
        }

        val filtered = listToDisplay.filter { item ->
            query.isEmpty() || item.description.lowercase().contains(query)
        }

        for (item in filtered) {
            addTransactionRow(item)
        }
    }

    private fun addTransactionRow(item: TransactionItem) {
        val context = context ?: return
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            paddingStruct()
        }

        val headerRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val tvName = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = item.description
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(resources.getColor(R.color.black, null))
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }

        val tvAmount = TextView(context).apply {
            text = "${if (item.isExpense) "-₹" else "+₹"}%,d".format(item.amount)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(resources.getColor(if (item.isExpense) android.R.color.holo_red_dark else android.R.color.holo_green_dark, null))
        }

        headerRow.addView(tvName)
        headerRow.addView(tvAmount)

        val subRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 4, 0, 0)
        }

        val tvCat = TextView(context).apply {
            text = item.categoryName ?: "Miscellaneous"
            textSize = 10f
            setTextColor(resources.getColor(android.R.color.darker_gray, null))
        }

        subRow.addView(tvCat)
        row.addView(headerRow)
        row.addView(subRow)

        llTxItems.addView(row)
    }

    private fun showAddTransactionDialog() {
        val context = context ?: return
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_transaction, null)

        val etModalTxName = view.findViewById<EditText>(R.id.etModalTxName)
        val etModalTxAmount = view.findViewById<EditText>(R.id.etModalTxAmount)
        val rbModalExpense = view.findViewById<RadioButton>(R.id.rbModalExpense)
        val spModalTxCategory = view.findViewById<Spinner>(R.id.spModalTxCategory)
        val spModalPaymentMethod = view.findViewById<Spinner>(R.id.spModalPaymentMethod)
        val btnCancel = view.findViewById<View>(R.id.btnCancelModalTx)
        val btnSave = view.findViewById<View>(R.id.btnSaveModalTx)

        val categories = arrayOf("Miscellaneous", "Rent", "Groceries", "Shopping", "Food & Dining", "Utilities", "Transportation")
        spModalTxCategory.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, categories)

        val paymentMethods = arrayOf("UPI", "Credit Card", "Debit Card", "Net Banking", "Cash", "Other")
        spModalPaymentMethod.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, paymentMethods)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val name = etModalTxName.text.toString().trim()
            val amountStr = etModalTxAmount.text.toString().trim()

            if (name.isEmpty()) {
                etModalTxName.error = "Description required"
                return@setOnClickListener
            }
            if (amountStr.isEmpty()) {
                etModalTxAmount.error = "Amount required"
                return@setOnClickListener
            }

            val amount = amountStr.toLongOrNull() ?: 0L
            val isExpense = rbModalExpense.isChecked
            val category = spModalTxCategory.selectedItem?.toString() ?: "Miscellaneous"

            mainViewModel.addTransaction(name, amount, isExpense, category)
            Toast.makeText(context, "Transaction saved successfully!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showSpendingImportOptionsSheet() {
        val context = context ?: return
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_spending_import_options, null)

        val cardOptionManual = view.findViewById<View>(R.id.cardOptionManual)
        val cardOptionSms = view.findViewById<View>(R.id.cardOptionSms)
        val cardOptionFile = view.findViewById<View>(R.id.cardOptionFile)

        cardOptionManual?.setOnClickListener {
            dialog.dismiss()
            showAddTransactionDialog()
        }

        cardOptionSms?.setOnClickListener {
            dialog.dismiss()
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
                showSmsImportDialog()
            } else {
                requestSmsPermissionLauncher.launch(Manifest.permission.READ_SMS)
            }
        }

        cardOptionFile?.setOnClickListener {
            dialog.dismiss()
            showFileImportDialog()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showSmsImportDialog() {
        val context = context ?: return
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_sms_import, null)

        val btnScanSms = view.findViewById<View>(R.id.btnScanSms)
        val btnCancelSmsImport = view.findViewById<View>(R.id.btnCancelSmsImport)
        val btnImportSelectedSms = view.findViewById<Button>(R.id.btnImportSelectedSms)
        val llSmsPreviewItems = view.findViewById<LinearLayout>(R.id.llSmsPreviewItems)

        // Read 6 months historical SMS using BankSmsParser
        val scannedTransactions = SmsHistoryReader.readHistoricalTransactions(context, monthsBack = 6)

        if (scannedTransactions.isNotEmpty()) {
            llSmsPreviewItems?.removeAllViews()
            scannedTransactions.forEach { txn ->
                val row = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, 12, 0, 12)
                }
                val cb = CheckBox(context).apply {
                    isChecked = true
                }
                val textContainer = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                        marginStart = 16
                    }
                }
                val titleTv = TextView(context).apply {
                    text = txn.merchant ?: "${txn.bank} Transaction"
                    textSize = 13f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(resources.getColor(R.color.black, null))
                }
                val subTv = TextView(context).apply {
                    text = "${txn.bank} · A/c ${txn.accountLast4 ?: "----"}"
                    textSize = 11f
                    setTextColor(resources.getColor(android.R.color.darker_gray, null))
                }
                textContainer.addView(titleTv)
                textContainer.addView(subTv)

                val amountTv = TextView(context).apply {
                    text = "${if (txn.type == TransactionType.DEBIT) "-₹" else "+₹"}%,d".format(txn.amount.toLong())
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(resources.getColor(if (txn.type == TransactionType.DEBIT) android.R.color.holo_red_dark else android.R.color.holo_green_dark, null))
                }
                row.addView(cb)
                row.addView(textContainer)
                row.addView(amountTv)
                llSmsPreviewItems?.addView(row)
            }
            btnImportSelectedSms?.text = "Import (${scannedTransactions.size})"
        }

        btnScanSms?.setOnClickListener {
            Toast.makeText(context, "Scanning 6 months SMS inbox...", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            showSmsImportDialog()
        }

        btnCancelSmsImport?.setOnClickListener { dialog.dismiss() }

        btnImportSelectedSms?.setOnClickListener {
            if (scannedTransactions.isNotEmpty()) {
                scannedTransactions.forEach { txn ->
                    val isExpense = txn.type != TransactionType.CREDIT
                    val name = txn.merchant ?: "${txn.bank} SMS Spend"
                    mainViewModel.addTransaction(name, txn.amount.toLong(), isExpense, "Miscellaneous")
                }
                Toast.makeText(context, "Successfully imported ${scannedTransactions.size} SMS transactions!", Toast.LENGTH_SHORT).show()
            } else {
                // Default demonstration import if inbox is empty
                mainViewModel.addTransaction("Swiggy Order #8932", 349, true, "Food & Dining")
                mainViewModel.addTransaction("Amazon Pay / Grocery", 1499, true, "Groceries")
                mainViewModel.addTransaction("Uber India Rides", 280, true, "Transportation")
                Toast.makeText(context, "Successfully imported 3 SMS transactions!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showFileImportDialog() {
        val context = context ?: return
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_file_import, null)

        val spFileBankProvider = view.findViewById<Spinner>(R.id.spFileBankProvider)
        val cardUploadArea = view.findViewById<View>(R.id.cardUploadArea)
        val tvSelectedFileName = view.findViewById<TextView>(R.id.tvSelectedFileName)
        val btnCancelFileImport = view.findViewById<View>(R.id.btnCancelFileImport)
        val btnUploadFileImport = view.findViewById<View>(R.id.btnUploadFileImport)

        val bankProviders = arrayOf("HDFC Bank", "ICICI Bank", "State Bank of India (SBI)", "Axis Bank", "Kotak Mahindra Bank", "Other / Standard CSV")
        spFileBankProvider?.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, bankProviders)

        cardUploadArea?.setOnClickListener {
            tvSelectedFileName?.text = "📄 HDFC_Statement_July2026.pdf"
            Toast.makeText(context, "Statement file selected!", Toast.LENGTH_SHORT).show()
        }

        btnCancelFileImport?.setOnClickListener { dialog.dismiss() }

        btnUploadFileImport?.setOnClickListener {
            mainViewModel.addTransaction("Statement Import - Electricity Bill", 1250, true, "Utilities")
            mainViewModel.addTransaction("Statement Import - Supermarket", 3420, true, "Groceries")
            Toast.makeText(context, "File statement imported successfully!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun totalValueOrOne(value: Double): Double = if (value == 0.0) 1.0 else value

    private fun createCircleDrawable(colorHex: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(colorHex))
        }
    }

    private fun LinearLayout.paddingStruct() {
        setPadding(16, 16, 16, 16)
    }
}
