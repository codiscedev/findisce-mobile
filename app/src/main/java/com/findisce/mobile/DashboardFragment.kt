package com.findisce.mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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

        val tvNetWorth = view.findViewById<TextView>(R.id.tvNetWorth)
        val tvMonthlySpending = view.findViewById<TextView>(R.id.tvMonthlySpending)
        val tvSafetyReserve = view.findViewById<TextView>(R.id.tvSafetyReserve)

        // Observe dashboard summaries
        mainViewModel.summary.observe(viewLifecycleOwner) { result ->
            result.onSuccess { summary ->
                tvNetWorth.text = "₹%,d".format(summary.estimatedNetWorth)
                tvMonthlySpending.text = "₹%,d".format(summary.monthlySpending)
                tvSafetyReserve.text = "₹%,d".format(summary.safetyReserve)
            }
        }

        // Fetch updates
        mainViewModel.fetchDashboardSummary()

        return view
    }
}
