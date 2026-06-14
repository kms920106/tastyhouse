package com.tastyhouse.webapi.notice.response;

import java.time.LocalDateTime;

public record NoticeListItemResponse(
    Long id,
    String title,
    String content,
    LocalDateTime createdAt
) {
    public static NoticeListItemResponse from(
        Long id,
        String title,
        String content,
        LocalDateTime createdAt
    ) {
        return new NoticeListItemResponse(
            id,
            title,
            content,
            createdAt
        );
    }
}
