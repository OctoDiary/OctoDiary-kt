package org.bxkr.octodiary.components.changelog


import androidx.compose.material.icons.Icons
import org.bxkr.octodiary.R


class Changelog32 : Changelog() {
    override val versionName: Int
        get() = R.string.c32_version_name
    override val versionShortname: Int
        get() = R.string.c32_version_shortname
    override val shortDescription: Int
        get() = R.string.c32_description
    override val elements = null
}


