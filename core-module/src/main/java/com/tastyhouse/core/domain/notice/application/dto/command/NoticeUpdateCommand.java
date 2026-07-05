package com.tastyhouse.core.domain.notice.application.dto.command;

public record NoticeUpdateCommand(
    String title,
    String content,
    boolean visible
) {

    public static NoticeUpdateCommand of(String title, String content, boolean visible) {
        return new NoticeUpdateCommand(title, content, visible);
    }
}
