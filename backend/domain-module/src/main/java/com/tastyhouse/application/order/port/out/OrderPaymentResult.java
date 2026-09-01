package com.tastyhouse.application.order.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.model.PaymentStatus;

/**
 * 주문 상세에 함께 노출하는 결제 요약 조회 결과.
 *
 * <p><b>챕터 07 개정 — {@code amount}는 {@code Amount} VO가 아니라 경계 타입 {@code Integer}다.</b>
 * 이전에는 {@code PAYMENT.amount} 컬럼이 {@code @Convert}로 {@code Amount} VO에 매핑되어 QueryDSL이
 * VO 타입 path를 생성한다는 이유로 그 타입을 그대로 받고 소비 모듈이 {@code value()}로 꺼냈다. 그러나
 * 그것은 <b>영속 매핑의 산물이지 읽기 계약의 설계가 아니다</b> — 읽기 계약은 경계 타입을 싣는다는
 * 규칙(ID VO 경계 규칙의 "읽기 포트 계약은 Long")과 어긋났고, api 모듈이 {@code Amount.value()}를
 * 호출하는 것이 {@code apiModuleShouldBeDomainModelFree}의 유일한 비-enum 위반이었다.
 *
 * <p>VO → {@code Integer} 언랩은 {@code OrderQueryDao}가 fetch 직후 재조립으로 수행한다
 * ({@code withResolvedShopThumbnailImageUrl}과 같은 형태 — {@code Projections.constructor}가 생성자
 * 직접 투영이라 변환을 투영식에 넣을 수 없다).
 */
public record OrderPaymentResult(
    Long id,
    PaymentMethod paymentMethod,
    PaymentStatus paymentStatus,
    Integer amount,
    String cardCompany,
    String cardNumber,
    LocalDateTime approvedAt,
    String receiptUrl
) {
}
