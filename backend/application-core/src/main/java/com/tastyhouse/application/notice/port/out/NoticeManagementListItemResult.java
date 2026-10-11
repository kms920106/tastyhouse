package com.tastyhouse.application.notice.port.out;

import java.time.LocalDateTime;

public record NoticeManagementListItemResult(
    Long id,
    String title,
    String content,
    boolean visible,
    LocalDateTime createdAt
) {
}
