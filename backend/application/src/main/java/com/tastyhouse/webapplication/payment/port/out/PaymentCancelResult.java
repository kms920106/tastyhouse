package com.tastyhouse.webapplication.payment.port.out;

/**
 * 결제 취소 결과 — 취소 결과 코드와 사용자 표시 메시지.
 *
 * <p><b>챕터 10</b>에서 신설. Command 경로의 반환값이라 공유 읽기 계약 패키지에 두지 않는다
 * (선례: ceo의 {@code ShopDeliveryAreaBulk*Result}). 값은 도메인 enum
 * {@code PaymentCancelCode}에서 {@code name()}·{@code getMessage()}로 뽑는데, {@code getMessage()}는
 * web-api에 허용된 accessor 목록(name·getDescription·getDisplayName) 밖이라 강등을 서비스에서
 * 끝내야 한다({@code apiModuleShouldOnlyReadDomainEnums}).
 */
public record PaymentCancelResult(
    String cancelCode,
    String message
) {
}
