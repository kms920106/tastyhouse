package com.tastyhouse.webapi.review.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.shared.page.PageResult;

@Schema(description = "최신 리뷰 페이지 응답")
public record ReviewLatestPageResponse(
    @Schema(description = "최신 리뷰 목록")
    List<ReviewLatestListItemResponse> content,

    @Schema(description = "현재 페이지 번호(0부터 시작)", example = "0")
    int page,

    @Schema(description = "페이지 크기", example = "10")
    int size,

    @Schema(description = "전체 요소 개수", example = "42")
    long totalElements
) {

    public static ReviewLatestPageResponse from(PageResult<ReviewLatestListItemResponse> pageResult) {
        return new ReviewLatestPageResponse(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements());
    }
}
