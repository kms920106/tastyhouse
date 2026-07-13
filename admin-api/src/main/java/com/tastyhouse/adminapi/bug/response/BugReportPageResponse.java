package com.tastyhouse.adminapi.bug.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.shared.page.PageResult;

@Schema(description = "버그 제보 목록 페이지 응답")
public record BugReportPageResponse(
    @Schema(description = "버그 제보 목록")
    List<BugReportListItemResponse> content,

    @Schema(description = "현재 페이지 번호", example = "0")
    int page,

    @Schema(description = "페이지 크기", example = "10")
    int size,

    @Schema(description = "전체 항목 수", example = "42")
    long totalElements
) {

    public static BugReportPageResponse from(PageResult<BugReportListItemResponse> pageResult) {
        return new BugReportPageResponse(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements());
    }
}
