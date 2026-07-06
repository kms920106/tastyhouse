package com.tastyhouse.core.domain.notice.application.dto;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.notice.domain.vo.NoticeId;

public record NoticeDetailDto(
    NoticeId noticeId,
    String title,
    String content,
    boolean visible,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static NoticeDetailDto from(
        NoticeId noticeId,
        String title,
        String content,
        boolean visible,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new NoticeDetailDto(noticeId, title, content, visible, createdAt, updatedAt);
    }
}
