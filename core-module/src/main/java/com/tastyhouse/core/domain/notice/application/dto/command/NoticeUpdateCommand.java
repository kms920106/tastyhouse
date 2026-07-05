package com.tastyhouse.core.domain.notice.application.dto.command;

public record NoticeUpdateCommand(
    String title,
    String content,
    boolean visible
) {}
