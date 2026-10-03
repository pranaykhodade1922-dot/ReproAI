package com.pranay.reproai.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineLarge = TextStyle(fontFamily=FontFamily.SansSerif, fontSize=22.sp, lineHeight=28.sp, fontWeight=FontWeight.SemiBold),
    headlineMedium = TextStyle(fontFamily=FontFamily.SansSerif, fontSize=22.sp, lineHeight=28.sp, fontWeight=FontWeight.SemiBold),
    titleLarge = TextStyle(fontFamily=FontFamily.SansSerif, fontSize=18.sp, lineHeight=24.sp, fontWeight=FontWeight.SemiBold),
    titleMedium = TextStyle(fontFamily=FontFamily.SansSerif, fontSize=15.sp, lineHeight=21.sp, fontWeight=FontWeight.SemiBold),
    bodyLarge = TextStyle(fontFamily=FontFamily.SansSerif, fontSize=14.sp, lineHeight=21.sp),
    bodyMedium = TextStyle(fontFamily=FontFamily.SansSerif, fontSize=14.sp, lineHeight=20.sp),
    bodySmall = TextStyle(fontFamily=FontFamily.SansSerif, fontSize=12.sp, lineHeight=17.sp),
    labelLarge = TextStyle(fontFamily=FontFamily.SansSerif, fontSize=14.sp, lineHeight=20.sp, fontWeight=FontWeight.SemiBold),
    labelMedium = TextStyle(fontFamily=FontFamily.SansSerif, fontSize=12.sp, lineHeight=16.sp, fontWeight=FontWeight.Medium),
    labelSmall = TextStyle(fontFamily=FontFamily.SansSerif, fontSize=11.sp, lineHeight=15.sp)
)
