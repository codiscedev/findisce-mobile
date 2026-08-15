package com.findisce.mobile.data.model.category

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class InvestmentCategoryResponseItem(
    val id: String?,
    val userId: String?,
    val name: String?,
    @SerializedName("isAppreciation") val isAppreciation: Boolean?,
    val rate: BigDecimal?
)
