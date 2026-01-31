package org.bxkr.octodiary.network


import androidx.compose.material.icons.Icons
data class Developer(
    val nickname: String,
    val githubLink: String?,
    val telegramLink: String?,
) {
    companion object {
        val developers = listOf(
            Developer(
                "bxkr",
                "https://github.com/bxkr",
                "https://t.me/qikel"
            ),
            Developer(
                "Den4ikSuperOstryyPer4ik",
                "https://github.com/Den4ikSuperOstryyPer4ik",
                "https://t.me/Den4ikSOP"
            )
        )
    }
}


