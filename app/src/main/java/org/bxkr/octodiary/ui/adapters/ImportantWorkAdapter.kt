package org.bxkr.octodiary.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.bxkr.octodiary.databinding.ItemImportantWorkBinding

class ImportantWorkAdapter(
    private val context: Context,
    private val works: List<String>
) :
    RecyclerView.Adapter<ImportantWorkAdapter.ImportantWorkViewHolder>() {

    class ImportantWorkViewHolder(
        ItemImportantWorkBinding: ItemImportantWorkBinding
    ) :
        RecyclerView.ViewHolder(ItemImportantWorkBinding.root) {
        private val binding = ItemImportantWorkBinding
        fun bind(work: String) {
            binding.workName.text = work
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImportantWorkViewHolder {
        val binding =
            ItemImportantWorkBinding.inflate(LayoutInflater.from(context), parent, false)
        return ImportantWorkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImportantWorkViewHolder, position: Int) {
        val work = works[position]
        holder.bind(work)
    }

    override fun getItemCount(): Int = works.size
}