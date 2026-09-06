package com.tastyhouse.application.notice.port.out;

public record NoticeSearchCondition(
    String title,
    String content,
    Boolean visible
) {

    public static NoticeSearchCondition of(String title, String content, Boolean visible) {
        return new NoticeSearchCondition(title, content, visible);
    }
}
