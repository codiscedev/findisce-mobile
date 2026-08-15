package com.findisce.mobile

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.findisce.mobile.moneyflow.*
import com.google.android.material.button.MaterialButton

class MoneyFlowFragment : Fragment() {

    private lateinit var tabSpending: MaterialButton
    private lateinit var tabBills: MaterialButton
    private lateinit var tabBudget: MaterialButton
    private lateinit var tabIncome: MaterialButton
    private lateinit var tabCreditCards: MaterialButton
    private lateinit var tabCashFlow: MaterialButton

    private lateinit var allTabs: List<MaterialButton>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_money_flow, container, false)

        tabSpending = view.findViewById(R.id.tabSpending)
        tabBills = view.findViewById(R.id.tabBills)
        tabBudget = view.findViewById(R.id.tabBudget)
        tabIncome = view.findViewById(R.id.tabIncome)
        tabCreditCards = view.findViewById(R.id.tabCreditCards)
        tabCashFlow = view.findViewById(R.id.tabCashFlow)

        allTabs = listOf(tabSpending, tabBills, tabBudget, tabIncome, tabCreditCards, tabCashFlow)

        tabSpending.setOnClickListener {
            selectTab(tabSpending)
            openFragment(SpendingFragment())
        }

        tabBills.setOnClickListener {
            selectTab(tabBills)
            openFragment(RecurringBillsFragment())
        }

        tabBudget.setOnClickListener {
            selectTab(tabBudget)
            openFragment(BudgetFragment())
        }

        tabIncome.setOnClickListener {
            selectTab(tabIncome)
            openFragment(IncomeFragment())
        }

        tabCreditCards.setOnClickListener {
            selectTab(tabCreditCards)
            openFragment(CreditCardsFragment())
        }

        tabCashFlow.setOnClickListener {
            selectTab(tabCashFlow)
            openFragment(CashFlowFragment())
        }

        // Default initial tab selection
        selectTab(tabSpending)
        openFragment(SpendingFragment())

        return view
    }

    private fun selectTab(selectedTab: MaterialButton) {
        for (tab in allTabs) {
            if (tab == selectedTab) {
                tab.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2563EB"))
                tab.setTextColor(Color.WHITE)
                tab.strokeWidth = 0
            } else {
                tab.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                tab.setTextColor(Color.parseColor("#4B5563"))
                tab.strokeColor = ColorStateList.valueOf(Color.parseColor("#E5E7EB"))
                tab.strokeWidth = 2
            }
        }
    }

    private fun openFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}
