package com.tastyhouse.application.payment.port.out;

import java.time.LocalDateTime;

/**
 * 결제 단건 화면 계약 — 금액 VO와 enum을 경계 타입으로 강등한 형태.
 *
 * <p><b>챕터 10</b>에서 신설. 공유 읽기 계약 {@code PaymentResult}를 그대로 쓸 수 없는 이유는
 * <b>금액이 값 객체</b>이기 때문이다 — {@code result.amount()}는 Money VO이고 응답에 실리는 것은
 * {@code amount().value()}다. VO 언랩과 enum 강등은 도메인 타입을 아는 일이므로 서비스에 남기고
 * ({@code apiModuleShouldBeDomainModelFree}·{@code apiModuleShouldOnlyReadDomainEnums}가 web-api의
 * 도메인 접근을 막는다) 강등된 값을 이 계약에 담아 넘긴다.
 *
 * <p>null 처리는 승격 전 동작을 그대로 보존한다 — 금액·결제수단·결제상태·PG사는 null이면 null이다.
 *
 * <p><b>금액은 이 계약에서도 계산되지 않는다</b> — VO에서 꺼낸 값을 그대로 나른다.
 */
public record PaymentViewResult(
    Long id,
    Long orderId,
    String paymentMethod,
    String paymentStatus,
    Integer amount,
    String pgProvider,
    String pgTid,
    String pgOrderId,
    String cardCompany,
    String cardNumber,
    Integer installmentMonths,
    LocalDateTime approvedAt,
    LocalDateTime cancelledAt,
    String cancelReason,
    String receiptUrl,
    LocalDateTime createdAt
) {
}
