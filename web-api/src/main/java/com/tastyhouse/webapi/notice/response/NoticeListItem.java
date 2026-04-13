package com.tastyhouse.webapi.notice.response;

import java.time.LocalDateTime;

public record NoticeListItem(
    Long id,
    String title,
    String content,
    LocalDateTime createdAt
) {
    public static NoticeListItem from(
        Long id,
        String title,
        String content,
        LocalDateTime createdAt
    ) {
        return new NoticeListItem(
            id,
            title,
            content,
            createdAt
        );
    }
}
