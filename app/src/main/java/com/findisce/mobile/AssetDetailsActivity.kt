package com.findisce.mobile

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.findisce.mobile.ui.viewmodel.MainViewModel

class AssetDetailsActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel

    private lateinit var backIcon: ImageView
    private lateinit var headerTitle: TextView

    private lateinit var btnAddAssetTop: View
    private lateinit var btnAddFirstAsset: View
    private lateinit var cardEmptyState: CardView
    private lateinit var llAssetItemsList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_asset_details)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        backIcon = findViewById(R.id.back_icon)
        headerTitle = findViewById(R.id.header_title)

        backIcon.setOnClickListener { finish() }
        headerTitle.text = "Assets Portfolio Overview"

        btnAddAssetTop = findViewById(R.id.btnAddAssetTop)
        btnAddFirstAsset = findViewById(R.id.btnAddFirstAsset)
        cardEmptyState = findViewById(R.id.cardEmptyState)
        llAssetItemsList = findViewById(R.id.llAssetItemsList)

        btnAddAssetTop.setOnClickListener { openChooseCategory() }
        btnAddFirstAsset.setOnClickListener { openChooseCategory() }

        // Observe wealth items filtered for Assets
        mainViewModel.wealthItems.observe(this) { result ->
            result.onSuccess { items ->
                val assetItems = items.filter { it.isAsset }

                if (assetItems.isEmpty()) {
                    cardEmptyState.visibility = View.VISIBLE
                    llAssetItemsList.visibility = View.GONE
                } else {
                    cardEmptyState.visibility = View.GONE
                    llAssetItemsList.visibility = View.VISIBLE
                    //populateAssetList(assetItems)
                }
            }
        }

        mainViewModel.fetchWealthItems()
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.fetchWealthItems()
    }

    private fun openChooseCategory() {
        val intent = Intent(this, ChooseCategoryActivity::class.java).apply {
            putExtra("RECORD_TYPE", "asset")
        }
        startActivity(intent)
    }
//
//    private fun populateAssetList(items: List<MainViewModel.WealthItem>) {
//        llAssetItemsList.removeAllViews()
//
//        for (item in items) {
//            val card = CardView(this).apply {
//                radius = 16f
//                cardElevation = 0f
//                setCardBackgroundColor(Color.WHITE)
//                val params = LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//                )
//                params.setMargins(0, 0, 0, 12)
//                layoutParams = params
//            }
//
//            val rowLayout = LinearLayout(this).apply {
//                orientation = LinearLayout.HORIZONTAL
//                setPadding(20, 16, 20, 16)
//            }
//
//            val infoLayout = LinearLayout(this).apply {
//                orientation = LinearLayout.VERTICAL
//                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
//            }
//
//            val tvName = TextView(this).apply {
//                text = item.name
//                textSize = 15f
//                typeface = android.graphics.Typeface.DEFAULT_BOLD
//                setTextColor(Color.parseColor("#111827"))
//            }
//
//            val tvCategory = TextView(this).apply {
//                text = item.categoryName ?: "Real Estate / Asset"
//                textSize = 12f
//                setTextColor(Color.parseColor("#6B7280"))
//            }
//
//            infoLayout.addView(tvName)
//            infoLayout.addView(tvCategory)
//
//            val tvAmount = TextView(this).apply {
//                text = "+₹%,d".format(item.amount)
//                textSize = 15f
//                typeface = android.graphics.Typeface.DEFAULT_BOLD
//                setTextColor(Color.parseColor("#166534"))
//            }
//
//            rowLayout.addView(infoLayout)
//            rowLayout.addView(tvAmount)
//            card.addView(rowLayout)
//
//            llAssetItemsList.addView(card)
//        }
//    }
}
