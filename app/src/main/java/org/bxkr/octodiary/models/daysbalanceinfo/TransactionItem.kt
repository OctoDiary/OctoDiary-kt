package org.bxkr.octodiary.models.daysbalanceinfo



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class TransactionItem(
    @SerializedName("complex_info")
    val complexInfo: ComplexInfo?,
    @SerializedName("dishes")
    val dishes: List<Dish>
)


