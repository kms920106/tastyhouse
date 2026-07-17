package com.tastyhouse.core.domain.notice.application.dto.result;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.notice.domain.vo.NoticeId;

public record NoticeDetailResult(
    NoticeId noticeId,
    String title,
    String content,
    boolean visible,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static NoticeDetailResult from(
        NoticeId noticeId,
        String title,
        String content,
        boolean visible,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new NoticeDetailResult(noticeId, title, content, visible, createdAt, updatedAt);
    }
}
