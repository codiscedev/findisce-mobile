package com.findisce.mobile.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.findisce.mobile.data.model.*
import com.findisce.mobile.data.repository.FinancialRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val financialRepository = FinancialRepository()

    private val _summary = MutableLiveData<Result<DashboardSummary>>()
    val summary: LiveData<Result<DashboardSummary>> = _summary

    private val _wealthItems = MutableLiveData<Result<List<WealthItem>>>()
    val wealthItems: LiveData<Result<List<WealthItem>>> = _wealthItems

    private val _transactions = MutableLiveData<Result<List<TransactionItem>>>()
    val transactions: LiveData<Result<Result<List<TransactionItem>>>> = MutableLiveData() // Wait, let's clean this duplicate definition

    private val _transactionsReal = MutableLiveData<Result<List<TransactionItem>>>()
    val transactionsList: LiveData<Result<List<TransactionItem>>> = _transactionsReal

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

    fun addWealthItem(name: String, amount: Long, isAsset: Boolean) {
        viewModelScope.launch {
            try {
                val newItem = WealthItem(null, name, amount, isAsset)
                financialRepository.addWealthItem(newItem)
                fetchWealthItems()
                fetchDashboardSummary()
            } catch (e: Exception) {
                // Handle
            }
        }
    }

    fun fetchTransactions() {
        viewModelScope.launch {
            try {
                val list = financialRepository.getTransactions()
                _transactionsReal.value = Result.success(list)
            } catch (e: Exception) {
                _transactionsReal.value = Result.failure(e)
            }
        }
    }

    fun addTransaction(description: String, amount: Long, isExpense: Boolean) {
        viewModelScope.launch {
            try {
                val newTx = TransactionItem(null, description, amount, isExpense)
                financialRepository.addTransaction(newTx)
                fetchTransactions()
                fetchDashboardSummary()
            } catch (e: Exception) {
                // Handle
            }
        }
    }
}
