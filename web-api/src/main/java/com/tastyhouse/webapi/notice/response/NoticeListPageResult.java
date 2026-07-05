package com.tastyhouse.webapi.notice.response;

import java.util.List;

public record NoticeListPageResult(List<NoticeListItemResponse> content, int page, int size, long totalElements) {
    public static NoticeListPageResult of(List<NoticeListItemResponse> content, int page, int size, long totalElements) {
        return new NoticeListPageResult(content, page, size, totalElements);
    }
}
