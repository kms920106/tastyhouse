package com.tastyhouse.core.domain.event.application.dto;

import com.querydsl.core.annotations.QueryProjection;

public record EventDetailDto(
    String bannerFilePath
) {
    @QueryProjection
    public EventDetailDto {
    }
}
