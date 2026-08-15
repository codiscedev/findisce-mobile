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

class AddDebtActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel

    private lateinit var backIcon: ImageView
    private lateinit var headerTitle: TextView
    private lateinit var btnBackToCategory: TextView

    private lateinit var etLoanCategory: EditText
    private lateinit var spLoanStatus: Spinner
    private lateinit var etLoanName: EditText
    private lateinit var etLenderName: EditText
    private lateinit var etPrincipal: EditText
    private lateinit var etOutstanding: EditText
    private lateinit var etInterestRate: EditText
    private lateinit var etTenureMonths: EditText
    private lateinit var etEmiAmount: EditText
    private lateinit var etStartDate: EditText
    private lateinit var etEndDate: EditText
    private lateinit var spInterestType: Spinner
    private lateinit var spPrepayment: Spinner
    private lateinit var etDebtNotes: EditText
    private lateinit var btnCancelDebt: View
    private lateinit var btnSaveDebt: View

    private var categoryName: String = "Home Loan"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_debt)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Home Loan"

        backIcon = findViewById(R.id.back_icon)
        headerTitle = findViewById(R.id.header_title)
        btnBackToCategory = findViewById(R.id.btnBackToCategory)

        backIcon.setOnClickListener { finish() }
        btnBackToCategory.setOnClickListener { finish() }
        headerTitle.text = "Add Financial Record"

        etLoanCategory = findViewById(R.id.etLoanCategory)
        spLoanStatus = findViewById(R.id.spLoanStatus)
        etLoanName = findViewById(R.id.etLoanName)
        etLenderName = findViewById(R.id.etLenderName)
        etPrincipal = findViewById(R.id.etPrincipal)
        etOutstanding = findViewById(R.id.etOutstanding)
        etInterestRate = findViewById(R.id.etInterestRate)
        etTenureMonths = findViewById(R.id.etTenureMonths)
        etEmiAmount = findViewById(R.id.etEmiAmount)
        etStartDate = findViewById(R.id.etStartDate)
        etEndDate = findViewById(R.id.etEndDate)
        spInterestType = findViewById(R.id.spInterestType)
        spPrepayment = findViewById(R.id.spPrepayment)
        etDebtNotes = findViewById(R.id.etDebtNotes)
        btnCancelDebt = findViewById(R.id.btnCancelDebt)
        btnSaveDebt = findViewById(R.id.btnSaveDebt)

        etLoanCategory.setText(categoryName)

        // Dropdown Adapters
        spLoanStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Active", "Closed", "Pending"))
        spInterestType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Floating Interest Rate", "Fixed Interest Rate"))
        spPrepayment.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Yes, Prepayment Allowed", "No Prepayment Allowed"))

        // Date Pickers
        etStartDate.setOnClickListener { showDatePicker(etStartDate) }
        etEndDate.setOnClickListener { showDatePicker(etEndDate) }

        btnCancelDebt.setOnClickListener { finish() }

        btnSaveDebt.setOnClickListener {
            val loanName = etLoanName.text.toString().trim()
            val lenderName = etLenderName.text.toString().trim()
            val principalStr = etPrincipal.text.toString().trim()
            val outstandingStr = etOutstanding.text.toString().trim()

            if (loanName.isEmpty()) {
                etLoanName.error = "Loan name is required"
                return@setOnClickListener
            }
            if (principalStr.isEmpty()) {
                etPrincipal.error = "Principal amount is required"
                return@setOnClickListener
            }

            val principal = principalStr.toLongOrNull() ?: 0L
            val outstanding = outstandingStr.toLongOrNull() ?: principal

            mainViewModel.createDebt(
                name = if (lenderName.isNotEmpty()) "$loanName ($lenderName)" else loanName,
                amount = outstanding
            )

            Toast.makeText(this, "Debt record saved successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showDatePicker(targetEditText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val dateStr = "%02d/%02d/%04d".format(selectedDay, selectedMonth + 1, selectedYear)
            targetEditText.setText(dateStr)
        }, year, month, day).show()
    }
}
