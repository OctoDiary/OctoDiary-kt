package org.bxkr.octodiary.ui

import android.content.Context
import android.util.AttributeSet
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.materialswitch.MaterialSwitch
import org.bxkr.octodiary.R

class MaterialSwitchPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : SwitchPreferenceCompat(context, attrs, defStyleAttr) {

    private lateinit var holder: PreferenceViewHolder

    init {
        layoutResource = R.layout.switch_preference

    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        this.holder = holder
        with(holder.itemView) {
            val switch = findViewById<MaterialSwitch>(R.id.switchWidget)
            switch.isChecked = isChecked
            switch.setOnCheckedChangeListener { _, isChecked ->
                setChecked(isChecked)
            }
        }
    }
}