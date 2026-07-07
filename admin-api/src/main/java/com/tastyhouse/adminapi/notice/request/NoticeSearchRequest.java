package com.tastyhouse.adminapi.notice.request;

public record NoticeSearchRequest(
    String title,
    String content,
    Boolean visible
) {
}
