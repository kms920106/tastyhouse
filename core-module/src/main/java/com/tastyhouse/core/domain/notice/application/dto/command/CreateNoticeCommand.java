package com.tastyhouse.core.domain.notice.application.dto.command;

public record CreateNoticeCommand(
    String title,
    String content,
    boolean visible
) {

    public static CreateNoticeCommand of(String title, String content, boolean visible) {
        return new CreateNoticeCommand(title, content, visible);
    }
}
