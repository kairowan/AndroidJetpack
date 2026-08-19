package com.kotlinmvvm.core.designsystem.theme

/**
 * @author 浩楠
 * @date 2026-3-11
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 跨平台颜色 token 集合
 */
data class AppColorTokens(
    val primaryArgb: Long,
    val secondaryArgb: Long,
    val backgroundArgb: Long,
    val surfaceArgb: Long,
    val errorArgb: Long,
    val onSurfaceVariantArgb: Long
)

/**
 *  描述: 跨平台单个文本样式 token
 */
data class AppTextStyleTokens(
    val fontSizeSp: Float,
    val lineHeightSp: Float,
    val fontWeight: Int,
    val letterSpacingSp: Float = 0f
)

/**
 *  描述: 跨平台排版 token 集合
 */
data class AppTypographyTokens(
    val displayLarge: AppTextStyleTokens,
    val headlineLarge: AppTextStyleTokens,
    val headlineMedium: AppTextStyleTokens,
    val titleLarge: AppTextStyleTokens,
    val titleMedium: AppTextStyleTokens,
    val bodyLarge: AppTextStyleTokens,
    val bodyMedium: AppTextStyleTokens,
    val bodySmall: AppTextStyleTokens,
    val titleSmall: AppTextStyleTokens,
    val labelLarge: AppTextStyleTokens
)

/**
 *  描述: 跨平台设计 token 入口
 */
object AppThemeTokens {
    val lightColors = AppColorTokens(
        primaryArgb = 0xFF1976D2,
        secondaryArgb = 0xFF03DAC6,
        backgroundArgb = 0xFFFFFBFE,
        surfaceArgb = 0xFFFFFBFE,
        errorArgb = 0xFFB00020,
        onSurfaceVariantArgb = 0xFF5F6368
    )

    val darkColors = AppColorTokens(
        primaryArgb = 0xFF90CAF9,
        secondaryArgb = 0xFF03DAC6,
        backgroundArgb = 0xFF121212,
        surfaceArgb = 0xFF121212,
        errorArgb = 0xFFCF6679,
        onSurfaceVariantArgb = 0xFFBDC1C6
    )

    val typography = AppTypographyTokens(
        displayLarge = AppTextStyleTokens(
            fontSizeSp = 57f,
            lineHeightSp = 64f,
            fontWeight = 400,
            letterSpacingSp = -0.25f
        ),
        headlineLarge = AppTextStyleTokens(
            fontSizeSp = 32f,
            lineHeightSp = 40f,
            fontWeight = 400
        ),
        headlineMedium = AppTextStyleTokens(
            fontSizeSp = 28f,
            lineHeightSp = 36f,
            fontWeight = 400
        ),
        titleLarge = AppTextStyleTokens(
            fontSizeSp = 22f,
            lineHeightSp = 28f,
            fontWeight = 700
        ),
        titleMedium = AppTextStyleTokens(
            fontSizeSp = 16f,
            lineHeightSp = 24f,
            fontWeight = 500,
            letterSpacingSp = 0.15f
        ),
        bodyLarge = AppTextStyleTokens(
            fontSizeSp = 16f,
            lineHeightSp = 24f,
            fontWeight = 400,
            letterSpacingSp = 0.5f
        ),
        bodyMedium = AppTextStyleTokens(
            fontSizeSp = 14f,
            lineHeightSp = 20f,
            fontWeight = 400,
            letterSpacingSp = 0.25f
        ),
        bodySmall = AppTextStyleTokens(
            fontSizeSp = 12f,
            lineHeightSp = 16f,
            fontWeight = 400,
            letterSpacingSp = 0.4f
        ),
        titleSmall = AppTextStyleTokens(
            fontSizeSp = 14f,
            lineHeightSp = 20f,
            fontWeight = 500,
            letterSpacingSp = 0.1f
        ),
        labelLarge = AppTextStyleTokens(
            fontSizeSp = 14f,
            lineHeightSp = 20f,
            fontWeight = 500,
            letterSpacingSp = 0.1f
        )
    )

    fun colors(darkTheme: Boolean): AppColorTokens {
        return if (darkTheme) darkColors else lightColors
    }
}
