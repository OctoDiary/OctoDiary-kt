package org.bxkr.octodiary.models.mealsmenucomplexes


import com.google.gson.annotations.SerializedName

data class ItemX(
    @SerializedName("calories")
    val calories: Int,
    @SerializedName("carbohydrates")
    val carbohydrates: Int,
    @SerializedName("code")
    val code: String,
    @SerializedName("fat")
    val fat: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("ingredients")
    val ingredients: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("price")
    val price: Int,
    @SerializedName("protein")
    val protein: Int,
    @SerializedName("weight")
    val weight: String
)