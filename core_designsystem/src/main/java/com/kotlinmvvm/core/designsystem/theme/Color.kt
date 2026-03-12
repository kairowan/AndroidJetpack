package com.kotlinmvvm.core.designsystem.theme

import androidx.compose.ui.graphics.Color

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

// AppThemeTokens stores Android-style ARGB values (0xAARRGGBB), not Compose ColorLongs.
private fun Long.toComposeColor(): Color = Color(toInt())

private val LightTokens = AppThemeTokens.lightColors
private val DarkTokens = AppThemeTokens.darkColors

val Primary = LightTokens.primaryArgb.toComposeColor()
val Secondary = LightTokens.secondaryArgb.toComposeColor()
val Background = LightTokens.backgroundArgb.toComposeColor()
val Surface = LightTokens.surfaceArgb.toComposeColor()
val Error = LightTokens.errorArgb.toComposeColor()
val OnSurfaceVariant = LightTokens.onSurfaceVariantArgb.toComposeColor()

val PrimaryDark = DarkTokens.primaryArgb.toComposeColor()
val SecondaryDark = DarkTokens.secondaryArgb.toComposeColor()
val BackgroundDark = DarkTokens.backgroundArgb.toComposeColor()
val SurfaceDark = DarkTokens.surfaceArgb.toComposeColor()
val ErrorDark = DarkTokens.errorArgb.toComposeColor()
val OnSurfaceVariantDark = DarkTokens.onSurfaceVariantArgb.toComposeColor()
