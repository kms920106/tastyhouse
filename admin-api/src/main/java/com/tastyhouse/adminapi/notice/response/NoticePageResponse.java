package com.tastyhouse.adminapi.notice.response;

import java.util.List;

import com.tastyhouse.core.shared.page.PageResult;

public record NoticePageResponse(
    List<NoticeListItemResponse> content,
    int page,
    int size,
    long totalElements
) {

    public static NoticePageResponse from(PageResult<NoticeListItemResponse> pageResult) {
        return new NoticePageResponse(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements());
    }
}
