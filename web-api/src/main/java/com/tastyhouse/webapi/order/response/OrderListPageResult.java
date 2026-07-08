package com.tastyhouse.webapi.order.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.webapi.member.response.OrderListItemResponse;

@Schema(description = "주문 목록 페이지 응답")
public record OrderListPageResult(
    @Schema(description = "주문 목록")
    List<OrderListItemResponse> content,

    @Schema(description = "현재 페이지 번호(0부터 시작)", example = "0")
    int page,

    @Schema(description = "페이지 크기", example = "10")
    int size,

    @Schema(description = "전체 요소 수", example = "42")
    long totalElements
) {
}
