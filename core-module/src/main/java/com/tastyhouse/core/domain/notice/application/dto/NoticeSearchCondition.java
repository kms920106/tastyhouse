package com.tastyhouse.core.domain.notice.application.dto;

public record NoticeSearchCondition(
    String title,
    String content,
    Boolean visible
) {
}
