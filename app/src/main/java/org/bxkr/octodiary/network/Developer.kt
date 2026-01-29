package org.bxkr.octodiary.network

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
                "pixwet",
                "https://github.com/pixwet",
                "https://t.me/pixwet"
            )
        )
    }
}