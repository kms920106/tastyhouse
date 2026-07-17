package com.tastyhouse.core.domain.event.application.dto.result;

import com.querydsl.core.annotations.QueryProjection;

public record EventDetailResult(
    String bannerFilePath
) {
    @QueryProjection
    public EventDetailResult {
    }
}
