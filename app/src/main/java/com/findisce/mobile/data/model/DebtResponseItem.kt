package com.findisce.mobile.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class DebtResponseItem(
    val id: String?,
    val name: String?,
    val loanName: String?,
    val principal: BigDecimal?,
    val outstanding: BigDecimal?,
    @SerializedName("outstanding_principal") val outstandingPrincipal: BigDecimal?,
    @SerializedName("emi_amount") val emiAmount: BigDecimal?,
    @SerializedName("interest_rate") val interestRate: BigDecimal?
) {
    fun getEffectiveOutstanding(): Long {
        return (outstanding ?: outstandingPrincipal ?: principal ?: BigDecimal.ZERO).toLong()
    }
}
