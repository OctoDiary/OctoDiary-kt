package org.bxkr.octodiary.models.mealsmenucomplexes


import com.google.gson.annotations.SerializedName

data class Item(
    @SerializedName("allowSelectItems")
    val allowSelectItems: Boolean,
    @SerializedName("endDate")
    val endDate: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("items")
    val items: List<ItemX>,
    @SerializedName("kind")
    val kind: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("paymentType")
    val paymentType: Int,
    @SerializedName("preorderAllowed")
    val preorderAllowed: Boolean,
    @SerializedName("price")
    val price: Int,
    @SerializedName("startDate")
    val startDate: String
)