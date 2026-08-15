package com.findisce.mobile

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.findisce.mobile.data.local.SessionManager
import com.findisce.mobile.ui.viewmodel.CategoryViewModel
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var sessionManager: SessionManager

    private lateinit var etFullName: EditText
    private lateinit var etEmailAddress: EditText
    private lateinit var etMobileNumber: EditText
    private lateinit var spCountry: Spinner
    private lateinit var spPreferredCurrency: Spinner
    private lateinit var etInflationRate: EditText
    private lateinit var btnSaveProfile: View

    private lateinit var tabAssetCategories: TextView
    private lateinit var tabDebtCategories: TextView
    private lateinit var tabInvestmentCategories: TextView
    private lateinit var tabGoalCategories: TextView
    private lateinit var tabEssentialCategories: TextView
    private lateinit var llCategoryManagerList: LinearLayout

    private lateinit var etNewCategoryName: EditText
    private lateinit var spNewCategoryType: Spinner
    private lateinit var etNewCategoryRate: EditText
    private lateinit var btnCreateCategory: View

    private lateinit var rgThemeMode: RadioGroup
    private lateinit var rbThemeLight: RadioButton
    private lateinit var rbThemeDark: RadioButton
    private lateinit var rbThemeSystem: RadioButton
    private lateinit var btnApplyTheme: View

    private lateinit var btnSignOutDevice: View
    private lateinit var btnSignOutAll: View

    private var activeCategoryTab: String = "ASSET"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        sessionManager = SessionManager(requireContext())
        categoryViewModel = ViewModelProvider(requireActivity())[CategoryViewModel::class.java]

        // Profile & Preferences Views
        etFullName = view.findViewById(R.id.etFullName)
        etEmailAddress = view.findViewById(R.id.etEmailAddress)
        etMobileNumber = view.findViewById(R.id.etMobileNumber)
        spCountry = view.findViewById(R.id.spCountry)
        spPreferredCurrency = view.findViewById(R.id.spPreferredCurrency)
        etInflationRate = view.findViewById(R.id.etInflationRate)
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile)

        // Pre-fill user details
        etFullName.setText(sessionManager.fetchName())
        etEmailAddress.setText(sessionManager.fetchEmail())

        spCountry.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, arrayOf("India 🇮🇳", "United States 🇺🇸", "United Kingdom 🇬🇧", "Singapore 🇸🇬"))
        spPreferredCurrency.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, arrayOf("INR (₹)", "USD ($)", "EUR (€)", "GBP (£)"))

        btnSaveProfile.setOnClickListener {
            Toast.makeText(context, "Profile preferences saved successfully!", Toast.LENGTH_SHORT).show()
        }

        // Reminders Action
        view.findViewById<View>(R.id.btnAddCustomReminder)?.setOnClickListener {
            Toast.makeText(context, "Custom reminder scheduler feature enabled!", Toast.LENGTH_SHORT).show()
        }

        // Category Manager Sub-Tabs Views
        tabAssetCategories = view.findViewById(R.id.tabAssetCategories)
        tabDebtCategories = view.findViewById(R.id.tabDebtCategories)
        tabInvestmentCategories = view.findViewById(R.id.tabInvestmentCategories)
        tabGoalCategories = view.findViewById(R.id.tabGoalCategories)
        tabEssentialCategories = view.findViewById(R.id.tabEssentialCategories)
        llCategoryManagerList = view.findViewById(R.id.llCategoryManagerList)

        etNewCategoryName = view.findViewById(R.id.etNewCategoryName)
        spNewCategoryType = view.findViewById(R.id.spNewCategoryType)
        etNewCategoryRate = view.findViewById(R.id.etNewCategoryRate)
        btnCreateCategory = view.findViewById(R.id.btnCreateCategory)

        spNewCategoryType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, arrayOf("APPRECIATION", "DEPRECIATION"))

        // Category Sub-Tabs Listeners
        tabAssetCategories.setOnClickListener { switchCategoryTab("ASSET") }
        tabDebtCategories.setOnClickListener { switchCategoryTab("DEBT") }
        tabInvestmentCategories.setOnClickListener { switchCategoryTab("INVESTMENT") }
        tabGoalCategories.setOnClickListener { switchCategoryTab("GOAL") }
        tabEssentialCategories.setOnClickListener { switchCategoryTab("ESSENTIAL") }

        btnCreateCategory.setOnClickListener {
            val name = etNewCategoryName.text.toString().trim()
            val rate = etNewCategoryRate.text.toString().toDoubleOrNull() ?: 8.0
            val type = spNewCategoryType.selectedItem?.toString() ?: "APPRECIATION"

            if (name.isEmpty()) {
                etNewCategoryName.error = "Category name is required"
                return@setOnClickListener
            }

            categoryViewModel.createAssetCategory(name, type, rate)
            Toast.makeText(context, "Category '$name' created successfully!", Toast.LENGTH_SHORT).show()
            etNewCategoryName.text?.clear()
            etNewCategoryRate.text?.clear()
        }

        // Theme Manager
        rgThemeMode = view.findViewById(R.id.rgThemeMode)
        rbThemeLight = view.findViewById(R.id.rbThemeLight)
        rbThemeDark = view.findViewById(R.id.rbThemeDark)
        rbThemeSystem = view.findViewById(R.id.rbThemeSystem)
        btnApplyTheme = view.findViewById(R.id.btnApplyTheme)

        rgThemeMode.setOnCheckedChangeListener { _, checkedId ->
            rbThemeLight.setBackgroundResource(if (checkedId == R.id.rbThemeLight) R.drawable.pill_tab_active else R.drawable.pill_tab_inactive)
            rbThemeDark.setBackgroundResource(if (checkedId == R.id.rbThemeDark) R.drawable.pill_tab_active else R.drawable.pill_tab_inactive)
            rbThemeSystem.setBackgroundResource(if (checkedId == R.id.rbThemeSystem) R.drawable.pill_tab_active else R.drawable.pill_tab_inactive)
        }

        btnApplyTheme.setOnClickListener {
            Toast.makeText(context, "Theme settings applied!", Toast.LENGTH_SHORT).show()
        }

        // Sign Out Actions
        btnSignOutDevice = view.findViewById(R.id.btnSignOutDevice)
        btnSignOutAll = view.findViewById(R.id.btnSignOutAll)

        btnSignOutDevice.setOnClickListener { performSignOut() }
        btnSignOutAll.setOnClickListener { performSignOut() }

        // Load Initial Category Tab Data
        observeCategories()
        switchCategoryTab("ASSET")

        return view
    }

    private fun switchCategoryTab(tab: String) {
        activeCategoryTab = tab

        val tabs = listOf(
            tabAssetCategories to "ASSET",
            tabDebtCategories to "DEBT",
            tabInvestmentCategories to "INVESTMENT",
            tabGoalCategories to "GOAL",
            tabEssentialCategories to "ESSENTIAL"
        )

        for ((view, tag) in tabs) {
            if (tag == tab) {
                view.setTextColor(Color.parseColor("#2563EB"))
                view.setBackgroundResource(R.drawable.pill_tab_active)
            } else {
                view.setTextColor(Color.parseColor("#6B7280"))
                view.background = null
            }
        }

        when (tab) {
            "ASSET" -> categoryViewModel.fetchAssetCategories()
            "DEBT" -> categoryViewModel.fetchDebtCategories()
            "INVESTMENT" -> categoryViewModel.fetchInvestmentCategories()
            "GOAL" -> categoryViewModel.fetchGoalCategories()
            "ESSENTIAL" -> categoryViewModel.fetchEssentialCategories()
        }
    }

    private fun observeCategories() {
        categoryViewModel.assetCategories.observe(viewLifecycleOwner) { result ->
            if (activeCategoryTab == "ASSET") {
                result.onSuccess { items ->
                    populateCategoryList(items.map { Triple(it.name ?: "Asset", it.isAppreciation ?: true, it.rate?.toDouble() ?: 8.0) })
                }
            }
        }
        categoryViewModel.debtCategories.observe(viewLifecycleOwner) { result ->
            if (activeCategoryTab == "DEBT") {
                result.onSuccess { items ->
                    populateCategoryList(items.map { Triple(it.name ?: "Debt", false, 0.0) })
                }
            }
        }
        categoryViewModel.investmentCategories.observe(viewLifecycleOwner) { result ->
            if (activeCategoryTab == "INVESTMENT") {
                result.onSuccess { items ->
                    populateCategoryList(items.map { Triple(it.name ?: "Investment", it.isAppreciation ?: true, it.rate?.toDouble() ?: 8.0) })
                }
            }
        }
        categoryViewModel.goalCategories.observe(viewLifecycleOwner) { result ->
            if (activeCategoryTab == "GOAL") {
                result.onSuccess { items ->
                    populateCategoryList(items.map { Triple(it.name ?: "Goal", true, 0.0) })
                }
            }
        }
        categoryViewModel.essentialCategories.observe(viewLifecycleOwner) { result ->
            if (activeCategoryTab == "ESSENTIAL") {
                result.onSuccess { items ->
                    populateCategoryList(items.map { Triple(it.name ?: "Essential", true, 0.0) })
                }
            }
        }
    }

    private fun populateCategoryList(items: List<Triple<String, Boolean, Double>>) {
        llCategoryManagerList.removeAllViews()
        val context = context ?: return

        for ((name, isAppreciation, rate) in items) {
            val itemRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(16, 12, 16, 12)
            }

            val textLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val titleRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val tvName = TextView(context).apply {
                text = name
                textSize = 13f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#111827"))
            }

            val tvTag = TextView(context).apply {
                text = " SYSTEM "
                textSize = 9f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#6B7280"))
                setBackgroundColor(Color.parseColor("#F3F4F6"))
                setPadding(6, 2, 6, 2)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(12, 0, 0, 0)
                layoutParams = params
            }

            titleRow.addView(tvName)
            titleRow.addView(tvTag)

            val tvSubtitle = TextView(context).apply {
                text = "${if (isAppreciation) "Appreciation" else "Depreciation"} · Rate: ${rate.toInt()}%"
                textSize = 11f
                setTextColor(Color.parseColor("#9CA3AF"))
            }

            textLayout.addView(titleRow)
            textLayout.addView(tvSubtitle)
            itemRow.addView(textLayout)

            llCategoryManagerList.addView(itemRow)
        }
    }

    private fun performSignOut() {
        context?.let { ctx ->
            SessionManager(ctx).clearSession()
        }
        FirebaseAuth.getInstance().signOut()

        Toast.makeText(context, "Signed Out Successfully.", Toast.LENGTH_SHORT).show()
        val intent = Intent(activity, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}
