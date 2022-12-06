package org.bxkr.octodiary.ui.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView


class RecyclerBaseAdapter<VH : RecyclerView.ViewHolder>(
    private val names: List<String>,
    private val mAdapter: RecyclerView.Adapter<VH>
) :
    BaseAdapter(), Filterable {

    class RecyclerFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?) = FilterResults()
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {}
    }

    override fun getItemViewType(position: Int): Int = mAdapter.getItemViewType(position)
    override fun getCount(): Int = mAdapter.itemCount
    override fun getItem(position: Int): String = names[position]
    override fun getItemId(position: Int): Long = mAdapter.getItemId(position)
    override fun getFilter(): Filter = RecyclerFilter()

    @Suppress("UNCHECKED_CAST")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var newConvertView = convertView
        val holder: VH
        if (newConvertView == null) {
            holder = mAdapter.createViewHolder(parent!!, getItemViewType(position))
            newConvertView = holder.itemView
            newConvertView.tag = holder
        } else {
            holder = newConvertView.tag as VH
        }
        mAdapter.bindViewHolder(holder, position)
        return holder.itemView
    }
}