package org.bxkr.octodiary.models.release

data class Release(
    val html_url: String,
    val tag_name: String,
    val name: String,
    val body: String,
    val assets: List<Asset>
)
