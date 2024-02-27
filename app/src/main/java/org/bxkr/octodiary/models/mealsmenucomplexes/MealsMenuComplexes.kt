package org.bxkr.octodiary.models.mealsmenucomplexes


import com.google.gson.annotations.SerializedName

data class MealsMenuComplexes(
    @SerializedName("items")
    val items: List<Item>
)