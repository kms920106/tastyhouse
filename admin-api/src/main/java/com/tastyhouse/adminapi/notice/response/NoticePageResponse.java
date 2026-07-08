package com.tastyhouse.adminapi.notice.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.shared.page.PageResult;

@Schema(description = "공지사항 목록 페이지 응답")
public record NoticePageResponse(
    @Schema(description = "공지사항 목록")
    List<NoticeListItemResponse> content,

    @Schema(description = "현재 페이지 번호", example = "0")
    int page,

    @Schema(description = "페이지 크기", example = "10")
    int size,

    @Schema(description = "전체 항목 수", example = "42")
    long totalElements
) {

    public static NoticePageResponse from(PageResult<NoticeListItemResponse> pageResult) {
        return new NoticePageResponse(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements());
    }
}
