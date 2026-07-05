package com.tastyhouse.webapi.notice.response;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.notice.application.dto.NoticeListItemDto;

public record NoticeListItemResponse(
    Long id,
    String title,
    String content,
    LocalDateTime createdAt
) {
    public static NoticeListItemResponse from(NoticeListItemDto dto) {
        return new NoticeListItemResponse(
            dto.id(),
            dto.title(),
            dto.content(),
            dto.createdAt()
        );
    }
}
