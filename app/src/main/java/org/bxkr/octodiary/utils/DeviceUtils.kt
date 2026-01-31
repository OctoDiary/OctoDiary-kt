package org.bxkr.octodiary.utils


import androidx.compose.material.icons.Icons
import android.content.Context
import android.content.res.Configuration

object DeviceUtils {
    
    fun isTablet(context: Context): Boolean {
        val configuration = context.resources.configuration
        return (configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }
    
    fun getScreenWidthDp(context: Context): Int {
        return context.resources.configuration.screenWidthDp
    }
    
    fun getScreenHeightDp(context: Context): Int {
        return context.resources.configuration.screenHeightDp
    }
    
    fun isLandscape(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }
    
    fun getColumnCount(context: Context): Int {
        return when {
            isTablet(context) && isLandscape(context) -> 3
            isTablet(context) -> 2
            else -> 1
        }
    }
    
    fun shouldUseTwoPane(context: Context): Boolean {
        val widthDp = getScreenWidthDp(context)
        return widthDp >= 600 // Tablet threshold
    }
    
    fun getContentPadding(context: Context): Int {
        return when {
            isTablet(context) -> 24
            else -> 16
        }
    }
}



