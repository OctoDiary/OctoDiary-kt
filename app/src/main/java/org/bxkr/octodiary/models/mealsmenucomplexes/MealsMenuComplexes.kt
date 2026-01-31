package org.bxkr.octodiary.models.mealsmenucomplexes



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class MealsMenuComplexes(
    @SerializedName("items")
    val items: List<Item>? = listOf(),
)


