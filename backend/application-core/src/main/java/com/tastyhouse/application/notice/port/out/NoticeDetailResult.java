package com.tastyhouse.application.notice.port.out;

import java.time.LocalDateTime;

public record NoticeDetailResult(
    Long id,
    String title,
    String content,
    boolean visible,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
