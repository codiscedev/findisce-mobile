package com.findisce.mobile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.findisce.mobile.data.model.*
import com.findisce.mobile.data.repository.FinancialRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val financialRepository = FinancialRepository(application)

    private val _summary = MutableLiveData<Result<DashboardSummary>>()
    val summary: LiveData<Result<DashboardSummary>> = _summary

    private val _wealthItems = MutableLiveData<Result<List<WealthItem>>>()
    val wealthItems: LiveData<Result<List<WealthItem>>> = _wealthItems

    private val _assetCategories = MutableLiveData<Result<List<AssetCategoryResponseItem>>>()
    val assetCategories: LiveData<Result<List<AssetCategoryResponseItem>>> = _assetCategories

    private val _debtCategories = MutableLiveData<Result<List<GenericCategoryItem>>>()
    val debtCategories: LiveData<Result<List<GenericCategoryItem>>> = _debtCategories

    private val _investmentCategories = MutableLiveData<Result<List<GenericCategoryItem>>>()
    val investmentCategories: LiveData<Result<List<GenericCategoryItem>>> = _investmentCategories

    private val _goalCategories = MutableLiveData<Result<List<GenericCategoryItem>>>()
    val goalCategories: LiveData<Result<List<GenericCategoryItem>>> = _goalCategories

    private val _essentialCategories = MutableLiveData<Result<List<GenericCategoryItem>>>()
    val essentialCategories: LiveData<Result<List<GenericCategoryItem>>> = _essentialCategories

    private val _transactions = MutableLiveData<Result<List<TransactionItem>>>()
    val transactionsList: LiveData<Result<List<TransactionItem>>> = _transactions

    fun fetchDashboardSummary() {
        viewModelScope.launch {
            try {
                val data = financialRepository.getDashboardSummary()
                _summary.value = Result.success(data)
            } catch (e: Exception) {
                _summary.value = Result.failure(e)
            }
        }
    }

    fun fetchAssetCategories() {
        viewModelScope.launch {
            try {
                val list = financialRepository.getAssetCategories()
                _assetCategories.value = Result.success(list)
            } catch (e: Exception) {
                _assetCategories.value = Result.failure(e)
            }
        }
    }

    fun fetchDebtCategories() {
        viewModelScope.launch {
            try {
                val list = financialRepository.getDebtCategories()
                _debtCategories.value = Result.success(list)
            } catch (e: Exception) {
                _debtCategories.value = Result.failure(e)
            }
        }
    }

    fun fetchInvestmentCategories() {
        viewModelScope.launch {
            try {
                val list = financialRepository.getInvestmentCategories()
                _investmentCategories.value = Result.success(list)
            } catch (e: Exception) {
                _investmentCategories.value = Result.failure(e)
            }
        }
    }

    fun fetchGoalCategories() {
        viewModelScope.launch {
            try {
                val list = financialRepository.getGoalCategories()
                _goalCategories.value = Result.success(list)
            } catch (e: Exception) {
                _goalCategories.value = Result.failure(e)
            }
        }
    }

    fun fetchEssentialCategories() {
        viewModelScope.launch {
            try {
                val list = financialRepository.getEssentialCategories()
                _essentialCategories.value = Result.success(list)
            } catch (e: Exception) {
                _essentialCategories.value = Result.failure(e)
            }
        }
    }

    fun fetchWealthItems() {
        viewModelScope.launch {
            try {
                val list = financialRepository.getWealthItems()
                _wealthItems.value = Result.success(list)
            } catch (e: Exception) {
                _wealthItems.value = Result.failure(e)
            }
        }
    }

    fun createAssetDetailed(
        categoryName: String,
        assetName: String,
        assetType: String,
        appreciationRate: Double,
        purchaseValue: Long,
        currentMarketValue: Long,
        propertyType: String?,
        note: String?
    ) {
        viewModelScope.launch {
            try {
                financialRepository.createAssetDetailed(
                    categoryName, assetName, assetType, appreciationRate,
                    purchaseValue, currentMarketValue, propertyType, note
                )
                fetchWealthItems()
                fetchDashboardSummary()
            } catch (e: Exception) {
                _wealthItems.value = Result.failure(e)
            }
        }
    }

    fun createAsset(name: String, amount: Long) {
        viewModelScope.launch {
            try {
                financialRepository.createAsset(name, amount)
                fetchWealthItems()
                fetchDashboardSummary()
            } catch (e: Exception) {
                _wealthItems.value = Result.failure(e)
            }
        }
    }

    fun createDebt(name: String, amount: Long) {
        viewModelScope.launch {
            try {
                financialRepository.createDebt(name, amount)
                fetchWealthItems()
                fetchDashboardSummary()
            } catch (e: Exception) {
                _wealthItems.value = Result.failure(e)
            }
        }
    }

    fun createInvestment(name: String, amount: Long) {
        viewModelScope.launch {
            try {
                financialRepository.createInvestment(name, amount)
                fetchWealthItems()
            } catch (e: Exception) {
                _wealthItems.value = Result.failure(e)
            }
        }
    }

    fun createGoal(name: String, amount: Long) {
        viewModelScope.launch {
            try {
                financialRepository.createGoal(name, amount)
                fetchWealthItems()
            } catch (e: Exception) {
                _wealthItems.value = Result.failure(e)
            }
        }
    }

    fun createEssential(name: String, amount: Long) {
        viewModelScope.launch {
            try {
                financialRepository.createEssential(name, amount)
                fetchWealthItems()
            } catch (e: Exception) {
                _wealthItems.value = Result.failure(e)
            }
        }
    }

    fun fetchTransactions() {
        viewModelScope.launch {
            try {
                val list = financialRepository.getTransactions()
                _transactions.value = Result.success(list)
            } catch (e: Exception) {
                _transactions.value = Result.failure(e)
            }
        }
    }

    fun addTransaction(description: String, amount: Long, isExpense: Boolean, categoryName: String? = null) {
        viewModelScope.launch {
            try {
                val newTx = TransactionItem(null, description, amount, isExpense, categoryName)
                financialRepository.addTransaction(newTx)
                fetchTransactions()
                fetchDashboardSummary()
            } catch (e: Exception) {
                _transactions.value = Result.failure(e)
            }
        }
    }
}
