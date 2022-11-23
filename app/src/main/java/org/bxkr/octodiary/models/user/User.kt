package org.bxkr.octodiary.models.user

data class User(

    val info: Info,
    val contextPersons: List<ContextPersons>,
    val experiments: Experiments,
    val firebaseExperiments: List<String>,
    val type: String,
    val description: String,
    val mobileSubscriptionStatus: String
)