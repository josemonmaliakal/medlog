package com.queryb.medlog.ui.utils

import androidx.compose.ui.graphics.Color

fun glucoseColor(value: Float?): Color {
    value ?: return Color(0xFF9CA3AF)
    return when {
        value < 70   -> Color(0xFF1E88E5)
        value <= 99  -> Color(0xFF00897B)
        value <= 125 -> Color(0xFFF57C00)
        else         -> Color(0xFFD32F2F)
    }
}

fun cholesterolColor(value: Float?): Color {
    value ?: return Color(0xFF9CA3AF)
    return when {
        value < 200  -> Color(0xFF00897B)
        value <= 239 -> Color(0xFFF57C00)
        else         -> Color(0xFFD32F2F)
    }
}