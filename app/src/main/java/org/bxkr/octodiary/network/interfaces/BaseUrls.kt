package org.bxkr.octodiary.network.interfaces


import androidx.compose.material.icons.Icons
import org.bxkr.octodiary.Diary

abstract class BaseUrls {
    abstract fun getBaseUrl(diary: Diary): String
}


