package com.tastyhouse.core.domain.notice.application.dto.command;

public record UpdateNoticeCommand(
    String title,
    String content,
    Boolean visible
) {}
