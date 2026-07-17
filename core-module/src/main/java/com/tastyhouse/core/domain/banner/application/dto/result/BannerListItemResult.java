package com.tastyhouse.core.domain.banner.application.dto.result;

import com.querydsl.core.annotations.QueryProjection;

public record BannerListItemResult(
    Long id,
    String title,
    String filePath,
    String linkUrl
) {
    @QueryProjection
    public BannerListItemResult {
    }
}
