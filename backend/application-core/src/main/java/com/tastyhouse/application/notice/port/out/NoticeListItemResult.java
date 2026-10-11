package com.tastyhouse.application.notice.port.out;

import java.time.LocalDateTime;

public record NoticeListItemResult(
    Long id,
    String title,
    String content,
    LocalDateTime createdAt
) {
}
