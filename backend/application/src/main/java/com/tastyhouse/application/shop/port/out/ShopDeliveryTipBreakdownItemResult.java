package com.tastyhouse.application.shop.port.out;

/**
 * 확정 배달팁의 항목별 근거 한 줄.
 *
 * <p><b>챕터 10</b>에서 신설. {@code label}은 <b>서버가 만든 완성 문구</b>다 — 적용 구간의 하한 금액을
 * 고르는 규칙({@code ShopDeliveryTipCalculator}와 동일한 선택 규칙)과 기본배달거리의 km 환산·천 단위
 * 콤마가 모두 이 문구를 만드는 계산이므로, 그 전부가 서비스에 남고 결과 문자열만 여기 담긴다. 금액이
 * 0인 항목을 넣지 않는 판정도 마찬가지다.
 *
 * <p>{@code amount}는 포맷하지 않은 정수다 — 금액 필드 자체의 표기 포맷은 프론트 담당이다.
 */
public record ShopDeliveryTipBreakdownItemResult(
    String label,
    int amount
) {
}
