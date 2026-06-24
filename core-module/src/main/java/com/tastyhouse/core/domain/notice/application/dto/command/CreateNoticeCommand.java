package com.tastyhouse.core.domain.notice.application.dto.command;

public record CreateNoticeCommand(
    String title,
    String content,
    boolean visible
) {}
