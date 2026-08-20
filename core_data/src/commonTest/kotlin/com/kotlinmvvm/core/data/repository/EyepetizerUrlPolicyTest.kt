package com.kotlinmvvm.core.data.repository

import com.kotlinmvvm.core.data.eyepetizer.toEyepetizerHttpsUrl
import com.kotlinmvvm.core.model.EyepetizerFeedItem
import com.kotlinmvvm.core.model.EyepetizerFeedSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class EyepetizerUrlPolicyTest {
    @Test
    fun resourceUrlsUpgradeKnownHostsAndRejectAuthorityTricks() {
        assertEquals(
            "https://img.kaiyanapp.com/cover.jpg",
            "http://img.kaiyanapp.com/cover.jpg".toEyepetizerHttpsUrl()
        )
        assertEquals(
            "https://ali-img.kaiyanapp.com/cover.jpg",
            "http://ali-img.kaiyanapp.com/cover.jpg".toEyepetizerHttpsUrl()
        )
        assertNull("https://img.kaiyanapp.com:443/cover.jpg".toEyepetizerHttpsUrl())
        assertNull("https://img.kaiyanapp.com@evil.example/cover.jpg".toEyepetizerHttpsUrl())
        assertNull("https://evil.example/cover.jpg".toEyepetizerHttpsUrl())
    }

    @Test
    fun pageRequestsAcceptRelativeAndKnownAbsoluteUrlsOnly() {
        assertEquals(
            "https://baobab.kaiyanapp.com/api/v4/tabs/selected?page=2",
            EyepetizerRequestFactory.create(
                source = EyepetizerFeedSource.HOME_SELECTED,
                nextPageUrl = "/api/v4/tabs/selected?page=2"
            ).url
        )
        assertEquals(
            "https://baobab.kaiyanapp.com/api/v4/tabs/selected?page=2",
            EyepetizerRequestFactory.create(
                source = EyepetizerFeedSource.HOME_SELECTED,
                nextPageUrl = "http://baobab.kaiyanapp.com/api/v4/tabs/selected?page=2"
            ).url
        )
        assertFailsWith<EyepetizerInvalidUrlException> {
            EyepetizerRequestFactory.create(
                source = EyepetizerFeedSource.HOME_SELECTED,
                nextPageUrl = "https://evil.example/page/2"
            )
        }
    }

    @Test
    fun sharedDecoderParsesTheSamePayloadOnAndroidAndIos() {
        val page = decodeEyepetizerPage(
            payloadText = """
                {
                  "itemList": [{
                    "type": "video",
                    "data": {
                      "dataType": "VideoBeanForClient",
                      "id": 7,
                      "title": "Shared parser",
                      "description": "One DTO parser for Android and iOS",
                      "playUrl": "https://baobab.kaiyanapp.com/video.mp4",
                      "duration": 30,
                      "category": "test",
                      "cover": {
                        "feed": "https://img.kaiyanapp.com/cover.jpg"
                      }
                    }
                  }],
                  "nextPageUrl": "https://baobab.kaiyanapp.com/api/v4/tabs/selected?page=2"
                }
            """.trimIndent(),
            requestUrl = "https://baobab.kaiyanapp.com/api/v4/tabs/selected"
        )

        assertEquals("Shared parser", (page.items.single() as EyepetizerFeedItem.Video).title)
        assertEquals(
            "https://baobab.kaiyanapp.com/api/v4/tabs/selected?page=2",
            page.continuationToken
        )
    }
}
