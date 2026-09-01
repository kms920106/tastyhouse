package com.tastyhouse.webapi.member.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.order.port.out.OrderListItemResult;

@Schema(description = "주문 목록 아이템")
public record OrderListItemResponse(
    @Schema(description = "주문 ID(PK)", example = "1")
    Long id,

    @Schema(description = "매장명", example = "맛있는 김밥집")
    String shopName,

    @Schema(description = "매장 썸네일 이미지 URL", example = "https://cdn.tastyhouse.com/shop/thumbnail/1.jpg")
    String shopThumbnailImageUrl,

    @Schema(description = "첫 번째 주문 상품명", example = "치즈김밥")
    String firstProductName,

    @Schema(description = "총 주문 상품 개수", example = "3")
    Integer totalItemCount,

    @Schema(description = "결제 금액", example = "15000")
    Integer amount,

    @Schema(description = "결제 상태", example = "PAID")
    String paymentStatus,

    @Schema(description = "결제 일시", example = "2026-01-01T00:00:00")
    LocalDateTime paymentDate,

    @Schema(description = "수령 예약 시각(슬롯 시작). null이면 즉시 주문입니다.", example = "2026-08-08T18:00:00")
    LocalDateTime scheduledAt
) {
    public static OrderListItemResponse from(OrderListItemResult result) {
        return new OrderListItemResponse(
            result.id(),
            result.shopName(),
            result.shopThumbnailImageUrl(),
            result.firstProductName(),
            result.totalItemCount(),
            result.amount(),
            result.paymentStatus().name(),
            result.paymentDate(),
            result.scheduledAt()
        );
    }
}
