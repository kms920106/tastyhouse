package com.tastyhouse.webapi.product.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.shared.page.PageResult;

@Schema(description = "오늘의 할인 상품 페이지 응답")
public record TodayDiscountProductPageResponse(
    @Schema(description = "오늘의 할인 상품 목록")
    List<TodayDiscountProductListItemResponse> content,
    @Schema(description = "현재 페이지 번호(0부터 시작)", example = "0")
    int page,
    @Schema(description = "페이지 크기", example = "20")
    int size,
    @Schema(description = "전체 요소 개수", example = "42")
    long totalElements
) {

    public static TodayDiscountProductPageResponse from(PageResult<TodayDiscountProductListItemResponse> pageResult) {
        return new TodayDiscountProductPageResponse(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements());
    }
}
