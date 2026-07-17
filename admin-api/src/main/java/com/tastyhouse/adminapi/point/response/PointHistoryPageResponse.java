package com.tastyhouse.adminapi.point.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.shared.page.PageResult;

@Schema(description = "포인트 이력 페이지 응답")
public record PointHistoryPageResponse(
    @Schema(description = "포인트 이력 목록")
    List<PointHistoryResponse> content,

    @Schema(description = "현재 페이지 번호", example = "0")
    int page,

    @Schema(description = "페이지 크기", example = "10")
    int size,

    @Schema(description = "전체 항목 수", example = "42")
    long totalElements
) {

    public static PointHistoryPageResponse from(PageResult<PointHistoryResponse> pageResult) {
        return new PointHistoryPageResponse(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements());
    }
}
