package com.findisce.mobile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.findisce.mobile.data.model.*
import com.findisce.mobile.data.model.category.AssetCategoryResponseItem
import com.findisce.mobile.data.model.category.DebtCategoryResponseItem
import com.findisce.mobile.data.model.category.EssentialCategoryResponseItem
import com.findisce.mobile.data.model.category.GoalCategoryResponseItem
import com.findisce.mobile.data.model.category.InvestmentCategoryResponseItem
import com.findisce.mobile.data.repository.FinancialRepository
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val financialRepository = FinancialRepository(application)

    private val _assetCategories = MutableLiveData<Result<List<AssetCategoryResponseItem>>>()
    val assetCategories: LiveData<Result<List<AssetCategoryResponseItem>>> = _assetCategories

    private val _debtCategories = MutableLiveData<Result<List<DebtCategoryResponseItem>>>()
    val debtCategories: LiveData<Result<List<DebtCategoryResponseItem>>> = _debtCategories

    private val _investmentCategories = MutableLiveData<Result<List<InvestmentCategoryResponseItem>>>()
    val investmentCategories: LiveData<Result<List<InvestmentCategoryResponseItem>>> = _investmentCategories

    private val _goalCategories = MutableLiveData<Result<List<GoalCategoryResponseItem>>>()
    val goalCategories: LiveData<Result<List<GoalCategoryResponseItem>>> = _goalCategories

    private val _essentialCategories = MutableLiveData<Result<List<EssentialCategoryResponseItem>>>()
    val essentialCategories: LiveData<Result<List<EssentialCategoryResponseItem>>> = _essentialCategories

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

    fun createAssetCategory(name: String, assetType: String, rate: Double) {
        viewModelScope.launch {
            try {
                fetchAssetCategories()
            } catch (_: Exception) {}
        }
    }
}
