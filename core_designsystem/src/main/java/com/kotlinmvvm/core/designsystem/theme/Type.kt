package com.kotlinmvvm.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * @author 浩楠
 *
 * @date 2026-2-25
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

private fun AppTextStyleTokens.toComposeTextStyle(): TextStyle {
    return TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = fontWeight.toComposeFontWeight(),
        fontSize = fontSizeSp.sp,
        lineHeight = lineHeightSp.sp,
        letterSpacing = letterSpacingSp.sp
    )
}

private fun Int.toComposeFontWeight(): FontWeight {
    return when (this) {
        700 -> FontWeight.Bold
        500 -> FontWeight.Medium
        else -> FontWeight.Normal
    }
}

private val TypographyTokens = AppThemeTokens.typography

val Typography = Typography(
    displayLarge = TypographyTokens.displayLarge.toComposeTextStyle(),
    headlineLarge = TypographyTokens.headlineLarge.toComposeTextStyle(),
    headlineMedium = TypographyTokens.headlineMedium.toComposeTextStyle(),
    titleLarge = TypographyTokens.titleLarge.toComposeTextStyle(),
    titleMedium = TypographyTokens.titleMedium.toComposeTextStyle(),
    titleSmall = TypographyTokens.titleSmall.toComposeTextStyle(),
    bodyLarge = TypographyTokens.bodyLarge.toComposeTextStyle(),
    bodyMedium = TypographyTokens.bodyMedium.toComposeTextStyle(),
    bodySmall = TypographyTokens.bodySmall.toComposeTextStyle(),
    labelLarge = TypographyTokens.labelLarge.toComposeTextStyle()
)
