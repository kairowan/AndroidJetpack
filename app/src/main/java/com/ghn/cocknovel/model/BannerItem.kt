package com.ghn.cocknovel.model

import com.stx.xhb.androidx.entity.BaseBannerInfo

data class BannerItem(
    val imagePath: String,
    val title: String = ""
) : BaseBannerInfo {
    override fun getXBannerUrl(): Any = imagePath

    override fun getXBannerTitle(): String = title
}
