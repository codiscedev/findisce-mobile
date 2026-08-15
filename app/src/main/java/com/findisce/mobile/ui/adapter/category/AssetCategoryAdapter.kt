package com.findisce.mobile.ui.adapter.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.findisce.mobile.R
import com.findisce.mobile.data.model.category.AssetCategoryResponseItem
import com.google.android.material.button.MaterialButton

class AssetCategoryAdapter(
    private var categories: List<AssetCategoryResponseItem>,
    private val onItemClick: (AssetCategoryResponseItem) -> Unit
) : RecyclerView.Adapter<AssetCategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnCategoryItem: MaterialButton = itemView.findViewById(R.id.btnCategoryItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_asset_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = categories[position]
        holder.btnCategoryItem.text = item.name ?: ""
        holder.btnCategoryItem.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<AssetCategoryResponseItem>) {
        this.categories = newCategories
        notifyDataSetChanged()
    }
}
