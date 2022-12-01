package org.bxkr.octodiary.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.bxkr.octodiary.databinding.ItemDiaryMarkBinding
import org.bxkr.octodiary.models.diary.Mark

class MarkDiaryAdapter(
    private val context: Context,
    private val marks: List<Mark>
) :
    RecyclerView.Adapter<MarkDiaryAdapter.MarkDiaryViewHolder>() {

    class MarkDiaryViewHolder(
        ItemDiaryMarkBinding: ItemDiaryMarkBinding
    ) :
        RecyclerView.ViewHolder(ItemDiaryMarkBinding.root) {
        private val binding = ItemDiaryMarkBinding
        fun bind(mark: Mark) {
            binding.iconButton.text = mark.value
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkDiaryViewHolder {
        val binding =
            ItemDiaryMarkBinding.inflate(LayoutInflater.from(context), parent, false)
        return MarkDiaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MarkDiaryViewHolder, position: Int) {
        val mark = marks[position]
        holder.bind(mark)
    }

    override fun getItemCount(): Int = marks.size
}