package com.tastyhouse.adminapi.order.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.shared.page.PageResult;

@Schema(description = "주문 목록 페이지 응답")
public record OrderPageResponse(
    @Schema(description = "주문 목록")
    List<OrderListItemResponse> content,

    @Schema(description = "현재 페이지 번호", example = "0")
    int page,

    @Schema(description = "페이지 크기", example = "10")
    int size,

    @Schema(description = "전체 항목 수", example = "42")
    long totalElements
) {

    public static OrderPageResponse from(PageResult<OrderListItemResponse> pageResult) {
        return new OrderPageResponse(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements());
    }
}
