package com.tastyhouse.adminapi.order.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 검색 요청")
public record OrderSearchRequest(
    @Schema(description = "가게 ID", example = "1")
    Long shopId,

    @Schema(description = "주문 상태", example = "CONFIRMED", allowableValues = {"PENDING", "CONFIRMED", "PREPARING", "COMPLETED", "CANCELLED"})
    String orderStatus,

    @Schema(description = "주문 방법", example = "TABLE", allowableValues = {"TABLE", "RESERVATION", "DELIVERY", "TAKEOUT"})
    String orderMethod,

    @Schema(description = "결제 상태", example = "COMPLETED", allowableValues = {"PENDING", "COMPLETED", "FAILED", "CANCELLED"})
    String paymentStatus,

    @Schema(description = "주문 번호 (부분 일치 검색)", example = "ORD20260101000001")
    String orderNumber,

    @Schema(description = "주문자 이름 (부분 일치 검색)", example = "홍길동")
    String ordererName,

    @Schema(description = "조회 시작 일시", example = "2026-01-01T00:00:00")
    LocalDateTime startDate,

    @Schema(description = "조회 종료 일시", example = "2026-01-31T23:59:59")
    LocalDateTime endDate
) {
}
