package com.findisce.mobile.wealth

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.findisce.mobile.R
import com.findisce.mobile.ui.viewmodel.MainViewModel
import java.util.Calendar

class AddAssetActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel

    private lateinit var backIcon: ImageView
    private lateinit var headerTitle: TextView

    private lateinit var etSelectedCategory: EditText
    private lateinit var spAssetType: Spinner
    private lateinit var etAppreciationRate: EditText
    private lateinit var lblAssetName: TextView
    private lateinit var etAssetName: EditText
    private lateinit var lblPropertyType: TextView
    private lateinit var spPropertyType: Spinner
    private lateinit var etPurchaseValue: EditText
    private lateinit var etPurchaseDate: EditText
    private lateinit var etCurrentMarketValue: EditText
    private lateinit var etNote: EditText
    private lateinit var btnCancelAssetForm: View
    private lateinit var btnSaveAssetForm: View

    private var categoryName: String = "Asset"
    private var isAppreciation: Boolean = true
    private var defaultRate: Double = 8.0
    private var recordType: String = "asset"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_asset)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // Retrieve Intent Extras
        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Asset"
        isAppreciation = intent.getBooleanExtra("IS_APPRECIATION", true)
        defaultRate = intent.getDoubleExtra("RATE", 8.0)
        recordType = intent.getStringExtra("RECORD_TYPE")?.lowercase() ?: "asset"

        // Toolbar Views
        backIcon = findViewById(R.id.back_icon)
        headerTitle = findViewById(R.id.header_title)

        backIcon.setOnClickListener { finish() }
        headerTitle.text = "Add $categoryName"

        // Form Views
        etSelectedCategory = findViewById(R.id.etSelectedCategory)
        spAssetType = findViewById(R.id.spAssetType)
        etAppreciationRate = findViewById(R.id.etAppreciationRate)
        lblAssetName = findViewById(R.id.lblAssetName)
        etAssetName = findViewById(R.id.etAssetName)
        lblPropertyType = findViewById(R.id.lblPropertyType)
        spPropertyType = findViewById(R.id.spPropertyType)
        etPurchaseValue = findViewById(R.id.etPurchaseValue)
        etPurchaseDate = findViewById(R.id.etPurchaseDate)
        etCurrentMarketValue = findViewById(R.id.etCurrentMarketValue)
        etNote = findViewById(R.id.etNote)
        btnCancelAssetForm = findViewById(R.id.btnCancelAssetForm)
        btnSaveAssetForm = findViewById(R.id.btnSaveAssetForm)

        // Pre-fill Category & Appreciation rate
        etSelectedCategory.setText(categoryName)
        etAppreciationRate.setText(defaultRate.toString())

        // Asset Type Spinner
        val assetTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("APPRECIATION", "DEPRECIATION"))
        spAssetType.adapter = assetTypeAdapter
        spAssetType.setSelection(if (isAppreciation) 0 else 1)

        // Date Picker for Purchase Date
        etPurchaseDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val dateStr = "%04d-%02d-%02d".format(selectedYear, selectedMonth + 1, selectedDay)
                etPurchaseDate.setText(dateStr)
            }, year, month, day).show()
        }

        // Customise Labels & Options based on Category Name
        when (categoryName.uppercase()) {
            "PROPERTY" -> {
                lblAssetName.text = "Property Name *"
                etAssetName.hint = "E.g. Sea-face 2BHK flat"
                lblPropertyType.text = "Property Type *"
                lblPropertyType.visibility = View.VISIBLE
                spPropertyType.visibility = View.VISIBLE
                spPropertyType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Apartment", "Villa", "Plot", "Commercial"))
            }
            "VEHICLE" -> {
                lblAssetName.text = "Vehicle Name *"
                etAssetName.hint = "E.g. Honda City 2024"
                lblPropertyType.text = "Vehicle Type *"
                lblPropertyType.visibility = View.VISIBLE
                spPropertyType.visibility = View.VISIBLE
                spPropertyType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Car", "Bike", "Electric Vehicle", "Commercial"))
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

        btnCancelAssetForm.setOnClickListener { finish() }

        btnSaveAssetForm.setOnClickListener {
            val assetName = etAssetName.text.toString().trim()
            val purchaseValStr = etPurchaseValue.text.toString().trim()
            val currentMarketValStr = etCurrentMarketValue.text.toString().trim()

            if (assetName.isEmpty()) {
                etAssetName.error = "Name is required"
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

            when (recordType) {
                "debt", "liability" -> {
                    mainViewModel.createDebt(assetName, currentMarketVal)
                }
                "investment" -> {
                    mainViewModel.createInvestment(assetName, currentMarketVal)
                }
                "goal" -> {
                    mainViewModel.createGoal(assetName, currentMarketVal)
                }
                "essential" -> {
                    mainViewModel.createEssential(assetName, currentMarketVal)
                }
                else -> {
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
                }
            }

            Toast.makeText(this, "$categoryName record saved successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}