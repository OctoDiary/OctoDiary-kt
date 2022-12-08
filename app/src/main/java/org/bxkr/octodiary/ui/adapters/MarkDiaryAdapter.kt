package org.bxkr.octodiary.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.bxkr.octodiary.databinding.ItemDiaryMarkBinding
import org.bxkr.octodiary.models.diary.Mark
import org.bxkr.octodiary.ui.activities.MainActivity
import org.bxkr.octodiary.ui.activities.MarkActivity

class MarkDiaryAdapter(
    private val context: Context,
    private val marks: List<Mark>
) :
    RecyclerView.Adapter<MarkDiaryAdapter.MarkDiaryViewHolder>() {

    class MarkDiaryViewHolder(
        ItemDiaryMarkBinding: ItemDiaryMarkBinding,
        context: Context
    ) :
        RecyclerView.ViewHolder(ItemDiaryMarkBinding.root) {
        private val parentContext = (context as MainActivity)
        private val binding = ItemDiaryMarkBinding
        fun bind(mark: Mark) {
            binding.iconButton.text = mark.value
            binding.iconButton.setOnClickListener {
                val intent = Intent(parentContext, MarkActivity::class.java)
                intent.putExtra(
                    "person_id",
                    parentContext.userData?.contextPersons?.get(0)?.personId
                )
                intent.putExtra(
                    "group_id",
                    parentContext.userData?.contextPersons?.get(0)?.group?.id
                )
                intent.putExtra("mark_id", mark.id)
                parentContext.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkDiaryViewHolder {
        val binding =
            ItemDiaryMarkBinding.inflate(LayoutInflater.from(context), parent, false)
        return MarkDiaryViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: MarkDiaryViewHolder, position: Int) {
        val mark = marks[position]
        holder.bind(mark)
    }

    override fun getItemCount(): Int = marks.size
}