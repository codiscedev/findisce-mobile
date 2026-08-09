package com.findisce.mobile.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class AssetResponseItem(
    val id: String?,
    val name: String?,
    val type: String?,
    @SerializedName("current_value") val currentValue: BigDecimal?,
    @SerializedName("current_market_value") val currentMarketValue: BigDecimal?,
    @SerializedName("purchase_value") val purchaseValue: BigDecimal?,
    @SerializedName("is_appreciation") val isAppreciation: Boolean?
) {
    fun getEffectiveValue(): Long {
        return (currentValue ?: currentMarketValue ?: purchaseValue ?: BigDecimal.ZERO).toLong()
    }
}
