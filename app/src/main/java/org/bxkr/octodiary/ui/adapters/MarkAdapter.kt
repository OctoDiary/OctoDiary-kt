package org.bxkr.octodiary.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.bxkr.octodiary.databinding.ItemMarkBinding
import org.bxkr.octodiary.models.shared.Mark
import org.bxkr.octodiary.models.shared.NamedMark
import org.bxkr.octodiary.ui.activities.MarkActivity

class MarkAdapter(
    private val context: Context,
    private val namedMarks: List<NamedMark>?,
    private val extended: Boolean = false,
    private val personId: Long,
    private val groupId: Long,
    private val simpleMarks: List<Mark>? = null
) : RecyclerView.Adapter<MarkAdapter.MarkDiaryViewHolder>() {

    class MarkDiaryViewHolder(
        ItemMarkBinding: ItemMarkBinding,
        context: Context,
        private val extended: Boolean,
        private val personId: Long,
        private val groupId: Long
    ) : RecyclerView.ViewHolder(ItemMarkBinding.root) {
        private val parentContext = context
        private val binding = ItemMarkBinding
        fun bind(namedMark: NamedMark? = null, simpleMark: Mark? = null) {
            val mark: Mark? = if (extended) {
                Mark(namedMark!!.id, null, namedMark.value)
            } else simpleMark
            binding.iconButton.text = mark!!.value
            val makeIconButtonNotClickable = {
                binding.iconButton.let {
                    it.isFocusable = false
                    it.isClickable = false
                }
            }
            if (mark.value.matches(Regex("""\d"""))) {
                binding.root.let {
                    it.isFocusable = true
                    it.isClickable = true
                    it.setOnClickListener { goToMark(mark) }
                }
                binding.iconButton.setOnClickListener { goToMark(mark) }
            } else makeIconButtonNotClickable()
            if (extended) {
                makeIconButtonNotClickable()
                binding.workType.visibility = View.VISIBLE
                binding.workType.text = namedMark!!.name
            }
        }

        private fun goToMark(mark: Mark) {
            val intent = Intent(parentContext, MarkActivity::class.java)
            intent.putExtra("person_id", personId)
            intent.putExtra("group_id", groupId)
            intent.putExtra("mark_id", mark.id)
            parentContext.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkDiaryViewHolder {
        val binding = ItemMarkBinding.inflate(LayoutInflater.from(context), parent, false)
        return MarkDiaryViewHolder(binding, context, extended, personId, groupId)
    }

    override fun onBindViewHolder(holder: MarkDiaryViewHolder, position: Int) {
        if (extended) {
            holder.bind(namedMark = namedMarks?.get(position)!!)
        } else {
            holder.bind(simpleMark = simpleMarks?.get(position)!!)
        }
    }

    override fun getItemCount(): Int {
        if (namedMarks != null) return namedMarks.size
        return simpleMarks!!.size
    }
}