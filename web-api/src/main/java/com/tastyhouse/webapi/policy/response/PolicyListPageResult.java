package com.tastyhouse.webapi.policy.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "정책 목록 페이지 응답")
public record PolicyListPageResult(
    @Schema(description = "정책 목록")
    List<PolicyListItemResponse> content,

    @Schema(description = "페이지 번호(0부터 시작)", example = "0")
    int page,

    @Schema(description = "페이지 크기", example = "10")
    int size,

    @Schema(description = "전체 요소 수", example = "42")
    long totalElements
) {
    public static PolicyListPageResult of(List<PolicyListItemResponse> content, int page, int size, long totalElements) {
        return new PolicyListPageResult(content, page, size, totalElements);
    }
}
