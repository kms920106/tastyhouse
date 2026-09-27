package com.tastyhouse.application.notice.port.out.write;

import java.time.LocalDateTime;

public record NoticeState(
    Long id,
    String title,
    String content,
    boolean visible,
    boolean deleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
