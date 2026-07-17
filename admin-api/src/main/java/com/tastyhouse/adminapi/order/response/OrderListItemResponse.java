package com.tastyhouse.adminapi.order.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.domain.order.application.dto.result.OrderAdminListItemResult;

@Schema(description = "주문 목록 항목 응답")
public record OrderListItemResponse(
    @Schema(description = "주문 ID", example = "1")
    Long id,

    @Schema(description = "주문 번호", example = "ORD20260101000001")
    String orderNumber,

    @Schema(description = "가게명", example = "BBQ치킨 성내점")
    String shopName,

    @Schema(description = "주문자 이름", example = "홍길동")
    String ordererName,

    @Schema(description = "주문 방법", example = "TABLE")
    String orderMethod,

    @Schema(description = "주문 상태", example = "CONFIRMED")
    String orderStatus,

    @Schema(description = "결제 상태", example = "COMPLETED")
    String paymentStatus,

    @Schema(description = "최종 결제 금액", example = "21000")
    Integer finalAmount,

    @Schema(description = "총 주문 상품 수량", example = "3")
    Integer totalItemCount,

    @Schema(description = "주문 생성 일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt
) {
    public static OrderListItemResponse from(OrderAdminListItemResult result) {
        return new OrderListItemResponse(
            result.id(),
            result.orderNumber(),
            result.shopName(),
            result.ordererName(),
            result.orderMethod() != null ? result.orderMethod().name() : null,
            result.orderStatus() != null ? result.orderStatus().name() : null,
            result.paymentStatus() != null ? result.paymentStatus().name() : null,
            result.finalAmount(),
            result.totalItemCount(),
            result.createdAt()
        );
    }
}
