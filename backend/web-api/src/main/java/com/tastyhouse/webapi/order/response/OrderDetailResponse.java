package com.tastyhouse.webapi.order.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 상세 조회 응답")
public record OrderDetailResponse(
    @Schema(description = "주문 ID", example = "1")
    Long id,

    @Schema(description = "주문 번호", example = "ORD20260101000001")
    String orderNumber,

    @Schema(description = "주문 방법", example = "TABLE")
    String orderMethod,

    @Schema(description = "결제 상태", example = "APPROVED")
    String paymentStatus,

    @Schema(description = "가게명", example = "BBQ치킨 성내점")
    String shopName,

    @Schema(description = "가게 전화번호", example = "0212345678")
    String shopPhoneNumber,

    @Schema(description = "주문자 이름", example = "홍길동")
    String ordererName,

    @Schema(description = "주문자 휴대폰 번호", example = "01011111111")
    String ordererPhone,

    @Schema(description = "주문자 이메일", example = "tastyhouse20@gmail.com")
    String ordererEmail,

    @Schema(description = "상품 금액 합계", example = "25000")
    Integer totalProductAmount,

    @Schema(description = "상품 할인 금액", example = "2000")
    Integer productDiscountAmount,

    @Schema(description = "쿠폰 할인 금액", example = "1000")
    Integer couponDiscountAmount,

    @Schema(description = "포인트 할인 금액", example = "1000")
    Integer pointDiscountAmount,

    @Schema(description = "총 할인 금액", example = "4000")
    Integer totalDiscountAmount,

    @Schema(description = "최종 결제 금액", example = "21000")
    Integer finalAmount,

    @Schema(description = "사용한 포인트", example = "1000")
    Integer usedPoint,

    @Schema(description = "적립된 포인트", example = "210")
    Integer earnedPoint,

    @Schema(description = "주문 상품 목록")
    List<OrderProductResponse> orderProducts,

    @Schema(description = "결제 요약 정보")
    PaymentSummaryResponse payment,

    @Schema(description = "결제 승인 일시", example = "2026-01-01T00:00:00")
    LocalDateTime approvedAt,

    @Schema(description = "주문 생성 일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt,

    @Schema(description = "수령 예약 시각(슬롯 시작). null이면 즉시 주문입니다.", example = "2026-08-08T18:00:00")
    LocalDateTime scheduledAt,

    @Schema(description = "수령 예약 슬롯 종료 시각. 포장은 scheduledAt과 동일하며, 즉시 주문은 null입니다.", example = "2026-08-08T18:30:00")
    LocalDateTime scheduledSlotEndAt
) {
    public static OrderDetailResponse from(
        Long id,
        String orderNumber,
        String orderMethod,
        String paymentStatus,
        String shopName,
        String shopPhoneNumber,
        String ordererName,
        String ordererPhone,
        String ordererEmail,
        Integer totalProductAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer pointDiscountAmount,
        Integer totalDiscountAmount,
        Integer finalAmount,
        Integer usedPoint,
        Integer earnedPoint,
        List<OrderProductResponse> orderProducts,
        PaymentSummaryResponse payment,
        LocalDateTime approvedAt,
        LocalDateTime createdAt,
        LocalDateTime scheduledAt,
        LocalDateTime scheduledSlotEndAt
    ) {
        return new OrderDetailResponse(
            id,
            orderNumber,
            orderMethod,
            paymentStatus,
            shopName,
            shopPhoneNumber,
            ordererName,
            ordererPhone,
            ordererEmail,
            totalProductAmount,
            productDiscountAmount,
            couponDiscountAmount,
            pointDiscountAmount,
            totalDiscountAmount,
            finalAmount,
            usedPoint,
            earnedPoint,
            orderProducts,
            payment,
            approvedAt,
            createdAt,
            scheduledAt,
            scheduledSlotEndAt
        );
    }
}
