package com.tastyhouse.adminapi.notice.response;

import java.util.List;

public record NoticePageResponse(
    List<NoticeListItemResponse> content,
    int page,
    int size,
    long totalElements
) {
}
