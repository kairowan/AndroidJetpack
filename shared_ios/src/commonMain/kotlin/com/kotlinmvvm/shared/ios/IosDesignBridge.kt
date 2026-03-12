package com.kotlinmvvm.shared.ios

import com.kotlinmvvm.core.designsystem.theme.AppThemeTokens
import com.kotlinmvvm.core.ui.model.UiComponentDefaults

/**
 * @author 浩楠
 * @date 2026-3-11
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: iOS 宿主消费共享设计 token 的桥接层
 */
class IosDesignBridge {
    fun snapshot(darkTheme: Boolean): IosThemeSnapshot {
        val colors = AppThemeTokens.colors(darkTheme)
        val typography = AppThemeTokens.typography
        val feedbackCopy = UiComponentDefaults.feedbackCopy
        return IosThemeSnapshot(
            primaryArgb = colors.primaryArgb,
            secondaryArgb = colors.secondaryArgb,
            backgroundArgb = colors.backgroundArgb,
            surfaceArgb = colors.surfaceArgb,
            errorArgb = colors.errorArgb,
            secondaryTextArgb = colors.onSurfaceVariantArgb,
            titleFontSizeSp = typography.titleLarge.fontSizeSp,
            bodyFontSizeSp = typography.bodyLarge.fontSizeSp,
            captionFontSizeSp = typography.bodySmall.fontSizeSp,
            errorTitle = feedbackCopy.errorTitle,
            retryLabel = feedbackCopy.retryLabel,
            emptyMessage = feedbackCopy.emptyMessage,
            noMoreMessage = feedbackCopy.noMoreMessage
        )
    }
}

/**
 *  描述: iOS 首页消费的主题快照
 */
data class IosThemeSnapshot(
    val primaryArgb: Long,
    val secondaryArgb: Long,
    val backgroundArgb: Long,
    val surfaceArgb: Long,
    val errorArgb: Long,
    val secondaryTextArgb: Long,
    val titleFontSizeSp: Float,
    val bodyFontSizeSp: Float,
    val captionFontSizeSp: Float,
    val errorTitle: String,
    val retryLabel: String,
    val emptyMessage: String,
    val noMoreMessage: String
)
