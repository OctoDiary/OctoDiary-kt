package org.bxkr.octodiary.models.mealbalance



import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class MealBalance(
    @SerializedName("balance")
    val balance: Int,
    @SerializedName("clientId")
    val clientId: ClientId,
    @SerializedName("foodboxAllowed")
    val foodboxAllowed: Boolean,
    @SerializedName("foodboxAvailable")
    val foodboxAvailable: Boolean,
    @SerializedName("organization")
    val organization: Organization,
    @SerializedName("preorderAllowed")
    val preorderAllowed: Boolean
)


