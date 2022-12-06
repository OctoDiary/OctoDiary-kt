package org.bxkr.octodiary.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import org.bxkr.octodiary.databinding.ItemSelectServerBinding
import org.bxkr.octodiary.network.NetworkService.Server

class SelectServerArrayAdapter(private val context: Context, private val servers: Array<Server>) :
    RecyclerView.Adapter<SelectServerArrayAdapter.ServersViewHolder>() {

    class ServersViewHolder(itemSelectServerBinding: ItemSelectServerBinding, context: Context) :
        RecyclerView.ViewHolder(itemSelectServerBinding.root) {
        private val binding = itemSelectServerBinding
        private val parentContext = context
        fun bind(server: Server) {
            binding.root.setCompoundDrawablesWithIntrinsicBounds(
                AppCompatResources.getDrawable(
                    parentContext,
                    server.drawableRes
                ), null, null, null
            )
            binding.root.text = parentContext.getString(server.serverName)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServersViewHolder {
        val binding = ItemSelectServerBinding.inflate(LayoutInflater.from(context), parent, false)
        return ServersViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: ServersViewHolder, position: Int) {
        val server = servers[position]
        holder.bind(server)
    }

    override fun getItemCount(): Int = servers.size
}