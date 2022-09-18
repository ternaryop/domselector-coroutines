package com.ternaryop.photoshelf.domselector

import com.ternaryop.photoshelf.domselector.util.html.DownloadOptions
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun Selector.toDownloadOptions(): DownloadOptions {
    val image = image;
    val cookie = image.cookie;

    val cookieResolved = if (cookie !== null) {
        val twoHoursFromNow: OffsetDateTime = OffsetDateTime
            .now(ZoneOffset.UTC)
            .plus(Duration.ofHours(12))
        val cookieDate = DateTimeFormatter.RFC_1123_DATE_TIME
            .format(twoHoursFromNow)
        cookie.replace("%cookie_date%", cookieDate)
    } else {
        null
    }
    return DownloadOptions(image.postData?.imgContinue, userAgent, cookieResolved)
}
