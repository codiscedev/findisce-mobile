package com.findisce.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.findisce.mobile.data.model.category.AssetCategoryResponseItem
import com.findisce.mobile.data.model.category.DebtCategoryResponseItem
import com.findisce.mobile.data.model.category.EssentialCategoryResponseItem
import com.findisce.mobile.data.model.category.GoalCategoryResponseItem
import com.findisce.mobile.data.model.category.InvestmentCategoryResponseItem
import com.findisce.mobile.ui.adapter.category.AssetCategoryAdapter
import com.findisce.mobile.ui.adapter.category.DebtCategoryAdapter
import com.findisce.mobile.ui.adapter.category.EssentialCategoryAdapter
import com.findisce.mobile.ui.adapter.category.GoalCategoryAdapter
import com.findisce.mobile.ui.adapter.category.InvestmentCategoryAdapter
import com.findisce.mobile.ui.viewmodel.CategoryViewModel
import com.findisce.mobile.ui.viewmodel.MainViewModel
import com.findisce.mobile.wealth.AddAssetActivity
import com.findisce.mobile.wealth.AddDebtActivity
import com.findisce.mobile.wealth.AddEssentialActivity
import com.findisce.mobile.wealth.AddGoalActivity
import com.findisce.mobile.wealth.AddInvestmentActivity

class ChooseCategoryActivity : AppCompatActivity() {

    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var mainViewModel: MainViewModel

    private lateinit var titleTextView: TextView
    private lateinit var backIconImageView: ImageView
    private lateinit var categoriesRecyclerView: RecyclerView

    private var recordType: String = "asset"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_choose_category)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recordType = intent.getStringExtra("RECORD_TYPE")?.lowercase() ?: "asset"

        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        titleTextView = findViewById(R.id.header_title)
        backIconImageView = findViewById(R.id.back_icon)
        categoriesRecyclerView = findViewById(R.id.categories)

        backIconImageView.setOnClickListener { finish() }

        setupUIForRecordType()
    }

    private fun setupUIForRecordType() {
        when (recordType) {
            "asset" -> {
                titleTextView.text = "Select Asset Category"
                setupAssetCategories()
            }
            "debt" -> {
                titleTextView.text = "Select Debt Category"
                setupDebtCategories()
            }
            "investment" -> {
                titleTextView.text = "Select Investment Category"
                setupInvestmentCategories()
            }
            "goal" -> {
                titleTextView.text = "Select Goal Category"
                setupGoalCategories()
            }
            "essential" -> {
                titleTextView.text = "Select Essential Category"
                setupEssentialCategories()
            }
            else -> {
                titleTextView.text = "Select Category"
                setupAssetCategories()
            }
        }
    }

    private fun setupAssetCategories() {
        val initialList = categoryViewModel.assetCategories.value?.getOrNull() ?: listOf()

        val adapter = AssetCategoryAdapter(initialList) { categoryItem ->
            openAddAsset(categoryItem)
        }

        categoriesRecyclerView.adapter = adapter

        categoryViewModel.fetchAssetCategories()
        categoryViewModel.assetCategories.observe(this) { result ->
            result.onSuccess { apiCategories ->
                if (apiCategories.isNotEmpty()) {
                    adapter.updateCategories(apiCategories)
                }
            }
        }
    }

    private fun setupDebtCategories() {
        val initialList = categoryViewModel.debtCategories.value?.getOrNull() ?: listOf()

        val adapter = DebtCategoryAdapter(initialList) { categoryItem ->
            openAddDebt(categoryItem)
        }

        categoriesRecyclerView.adapter = adapter

        categoryViewModel.fetchDebtCategories()
        categoryViewModel.debtCategories.observe(this) { result ->
            result.onSuccess { apiCategories ->
                if (apiCategories.isNotEmpty()) {
                    adapter.updateCategories(apiCategories)
                }
            }
        }
    }

    private fun setupInvestmentCategories() {
        val initialList = categoryViewModel.investmentCategories.value?.getOrNull() ?: listOf()

        val adapter = InvestmentCategoryAdapter(initialList) { categoryItem ->
            openAddInvestment(categoryItem)
        }

        categoriesRecyclerView.adapter = adapter

        categoryViewModel.fetchInvestmentCategories()
        categoryViewModel.investmentCategories.observe(this) { result ->
            result.onSuccess { apiCategories ->
                if (apiCategories.isNotEmpty()) {
                    adapter.updateCategories(apiCategories)
                }
            }
        }
    }

    private fun setupGoalCategories() {
        val initialList = categoryViewModel.goalCategories.value?.getOrNull() ?: listOf()

        val adapter = GoalCategoryAdapter(initialList) { categoryItem ->
            openAddGoal(categoryItem)
        }

        categoriesRecyclerView.adapter = adapter

        categoryViewModel.fetchGoalCategories()
        categoryViewModel.goalCategories.observe(this) { result ->
            result.onSuccess { apiCategories ->
                if (apiCategories.isNotEmpty()) {
                    adapter.updateCategories(apiCategories)
                }
            }
        }
    }

    private fun setupEssentialCategories() {
        val initialList = categoryViewModel.essentialCategories.value?.getOrNull() ?: listOf()

        val adapter = EssentialCategoryAdapter(initialList) { categoryItem ->
            openAddEssential(categoryItem)
        }

        categoriesRecyclerView.adapter = adapter

        categoryViewModel.fetchEssentialCategories()
        categoryViewModel.essentialCategories.observe(this) { result ->
            result.onSuccess { apiCategories ->
                if (apiCategories.isNotEmpty()) {
                    adapter.updateCategories(apiCategories)
                }
            }
        }
    }

    private fun openAddAsset(categoryItem: AssetCategoryResponseItem) {
        val intent = Intent(this, AddAssetActivity::class.java).apply {
            putExtra("RECORD_TYPE", "asset")
            putExtra("CATEGORY_NAME", categoryItem.name ?: "Asset")
            putExtra("IS_APPRECIATION", categoryItem.isAppreciation ?: true)
            putExtra("RATE", categoryItem.rate?.toDouble() ?: 8.0)
        }
        startActivity(intent)
    }

    private fun openAddDebt(categoryItem: DebtCategoryResponseItem) {
        val intent = Intent(this, AddDebtActivity::class.java).apply {
            putExtra("RECORD_TYPE", "debt")
            putExtra("CATEGORY_NAME", categoryItem.name ?: "Debt")
        }
        startActivity(intent)
    }

    private fun openAddInvestment(categoryItem: InvestmentCategoryResponseItem) {
        val intent = Intent(this, AddInvestmentActivity::class.java).apply {
            putExtra("RECORD_TYPE", "investment")
            putExtra("CATEGORY_NAME", categoryItem.name ?: "Investment")
            putExtra("IS_APPRECIATION", categoryItem.isAppreciation ?: true)
            putExtra("RATE", categoryItem.rate?.toDouble() ?: 8.0)
        }
        startActivity(intent)
    }

    private fun openAddGoal(categoryItem: GoalCategoryResponseItem) {
        val intent = Intent(this, AddGoalActivity::class.java).apply {
            putExtra("RECORD_TYPE", "goal")
            putExtra("CATEGORY_NAME", categoryItem.name ?: "Goal")
        }
        startActivity(intent)
    }

    private fun openAddEssential(categoryItem: EssentialCategoryResponseItem) {
        val intent = Intent(this, AddEssentialActivity::class.java).apply {
            putExtra("RECORD_TYPE", "essential")
            putExtra("CATEGORY_NAME", categoryItem.name ?: "Essential")
            putExtra("IS_APPRECIATION", categoryItem.isAppreciation ?: true)
            putExtra("RATE", categoryItem.rate?.toDouble() ?: 8.0)
        }
        startActivity(intent)
    }
}