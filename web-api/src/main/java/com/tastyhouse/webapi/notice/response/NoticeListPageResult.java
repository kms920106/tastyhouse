package com.tastyhouse.webapi.notice.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공지사항 목록 페이지 응답")
public record NoticeListPageResult(
    @Schema(description = "공지사항 목록")
    List<NoticeListItemResponse> content,

    @Schema(description = "현재 페이지 번호", example = "0")
    int page,

    @Schema(description = "페이지 크기", example = "10")
    int size,

    @Schema(description = "전체 항목 수", example = "42")
    long totalElements
) {
    public static NoticeListPageResult of(List<NoticeListItemResponse> content, int page, int size, long totalElements) {
        return new NoticeListPageResult(content, page, size, totalElements);
    }
}
