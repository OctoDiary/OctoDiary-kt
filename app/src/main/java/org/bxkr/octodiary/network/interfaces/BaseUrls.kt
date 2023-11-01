package org.bxkr.octodiary.network.interfaces

import org.bxkr.octodiary.Diary

abstract class BaseUrls {
    abstract fun getBaseUrl(diary: Diary): String
}