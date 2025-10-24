package org.bxkr.octodiary.components.changelog

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import org.bxkr.octodiary.BuildConfig

abstract class Changelog {
    abstract val elements: List<ChangelogItem>?

    @get:StringRes
    abstract val versionName: Int

    @get:StringRes
    abstract val versionShortname: Int

    @get:StringRes
    abstract val shortDescription: Int

    data class ChangelogItem(
        val composable: @Composable () -> Unit,
        @StringRes val title: Int,
        @StringRes val subtitle: Int,
    )

    companion object {
        val currentChangelog: Changelog =
            when (BuildConfig.VERSION_CODE) {
//                in 26..31 -> Changelog26()
//                in 32..32 -> Changelog32()
                in 33..Int.MAX_VALUE -> Changelog33()
                else -> Changelog26()
            }
    }
}