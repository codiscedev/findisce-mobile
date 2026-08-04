package com.findisce.mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class TaxPlannerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tax_planner, container, false)

        val etDeduction80C = view.findViewById<EditText>(R.id.etDeduction80C)
        val etDeduction80D = view.findViewById<EditText>(R.id.etDeduction80D)
        val btnRecalculateTax = view.findViewById<Button>(R.id.btnRecalculateTax)

        btnRecalculateTax.setOnClickListener {
            val cVal = etDeduction80C.text.toString().toLongOrNull() ?: 0L
            val dVal = etDeduction80D.text.toString().toLongOrNull() ?: 0L

            val totalDeductions = cVal + dVal
            if (totalDeductions > 180000L) {
                Toast.makeText(context, "Old Tax Regime is recommended with high deductions!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "New Tax Regime is recommended for lower declarations.", Toast.LENGTH_LONG).show()
            }
        }

        return view
    }
}
