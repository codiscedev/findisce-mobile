package com.findisce.mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.findisce.mobile.data.local.SessionManager
import com.findisce.mobile.ui.viewmodel.MainViewModel

class DashboardFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        val tvWelcomeName = view.findViewById<TextView>(R.id.tvWelcomeName)
        val tvNetWorth = view.findViewById<TextView>(R.id.tvNetWorth)
        val tvMonthlyIncome = view.findViewById<TextView>(R.id.tvMonthlyIncome)
        val tvMonthlyExpenses = view.findViewById<TextView>(R.id.tvMonthlyExpenses)
        val tvMonthlyNetSavings = view.findViewById<TextView>(R.id.tvMonthlyNetSavings)

        val btnActionTransfer = view.findViewById<View>(R.id.btnActionTransfer)
        val btnActionTax = view.findViewById<View>(R.id.btnActionTax)
        val btnActionConsultant = view.findViewById<View>(R.id.btnActionConsultant)

        // Populate session user name
        context?.let { ctx ->
            val name = SessionManager(ctx).fetchName() ?: SessionManager(ctx).fetchEmail()?.substringBefore("@") ?: "User"
            tvWelcomeName.text = "Welcome back, $name"
        }

        // Action Click Listeners
        btnActionTransfer?.setOnClickListener {
            Toast.makeText(context, "Opening Transfer & Move Wealth...", Toast.LENGTH_SHORT).show()
        }
        btnActionTax?.setOnClickListener {
            Toast.makeText(context, "Opening Tax Projections...", Toast.LENGTH_SHORT).show()
        }
        btnActionConsultant?.setOnClickListener {
            Toast.makeText(context, "Requesting Consultant Call...", Toast.LENGTH_SHORT).show()
        }

        // Observe dashboard summaries
        mainViewModel.summary.observe(viewLifecycleOwner) { result ->
            result.onSuccess { summary ->
                tvNetWorth.text = "₹%,d".format(summary.estimatedNetWorth)
                tvMonthlyExpenses.text = "₹%,d".format(summary.monthlySpending)
                tvMonthlyIncome.text = "₹%,d".format(summary.safetyReserve * 2)
                tvMonthlyNetSavings.text = "+₹%,d".format(summary.safetyReserve)
            }
        }

        // Fetch updates
        mainViewModel.fetchDashboardSummary()

        return view
    }
}
