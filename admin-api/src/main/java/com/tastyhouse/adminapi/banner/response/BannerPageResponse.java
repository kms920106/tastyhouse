package com.tastyhouse.adminapi.banner.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.shared.page.PageResult;

@Schema(description = "배너 목록 페이지 응답")
public record BannerPageResponse(
    @Schema(description = "배너 목록")
    List<BannerListItemResponse> content,

    @Schema(description = "페이지 번호(0부터 시작)", example = "0")
    int page,

    @Schema(description = "페이지 크기", example = "10")
    int size,

    @Schema(description = "전체 요소 수", example = "42")
    long totalElements
) {

    public static BannerPageResponse from(PageResult<BannerListItemResponse> pageResult) {
        return new BannerPageResponse(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements());
    }
}
