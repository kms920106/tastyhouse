package com.tastyhouse.core.domain.notice.application.dto.command;

public record NoticeCreateCommand(
    String title,
    String content,
    boolean visible
) {

    public static NoticeCreateCommand of(String title, String content, boolean visible) {
        return new NoticeCreateCommand(title, content, visible);
    }
}
