package com.tastyhouse.application.order.port.out;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 상세 화면 계약 — 공유 투영에 리뷰 여부·결제 요약을 합친 결과.
 *
 * <p><b>챕터 10</b>에서 신설. 공유 읽기 계약 {@code OrderDetailResult}를 쓰지 못하는 이유는 하위
 * 두 계약과 같다 — 주문상품에는 리뷰 컨텍스트에 물어본 {@code reviewed}가 붙고
 * ({@link OrderProductViewResult}), 결제 요약에는 주문의 보증금액이 합쳐진다
 * ({@link OrderPaymentSummaryResult}).
 *
 * <p><b>금액 필드는 전부 {@code OrderDetailResult}의 값을 그대로 옮겨 담는다 — 이 계약은 어떤 금액도
 * 계산하지 않는다.</b> 합계·할인 분해·보증금·최종금액의 산출은 주문 도메인과 DAO 투영이 이미 끝낸
 * 것이고, 이 챕터는 거처만 옮긴다(주문 총액의 유일한 가산항은 배달팁이라는 불변식도 그대로다).
 *
 * <p>{@code paymentStatus}가 별도 필드인 것은 기존 동작을 보존한 것이다 — 결제가 없거나 상태가 비어
 * 있으면 {@code null}이며, 결제 요약 안의 상태와 별개로 상세 최상단에도 실린다.
 */
public record OrderDetailViewResult(
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
    Integer cupDepositAmount,
    Integer finalAmount,
    Integer usedPoint,
    Integer earnedPoint,
    List<OrderProductViewResult> orderProducts,
    OrderPaymentSummaryResult payment,
    LocalDateTime paymentApprovedAt,
    LocalDateTime createdAt,
    LocalDateTime scheduledAt,
    LocalDateTime scheduledSlotEndAt
) {
}
