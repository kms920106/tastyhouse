package com.tastyhouse.infrastructure.mybatis.notice.persistence;

import java.time.LocalDateTime;

record NoticeRow(
    Long id,
    String title,
    String content,
    boolean visible,
    boolean deleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
