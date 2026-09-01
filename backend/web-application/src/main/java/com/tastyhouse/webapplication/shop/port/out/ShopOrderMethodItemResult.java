package com.tastyhouse.webapplication.shop.port.out;

/**
 * 주문 방식 한 건 — 배정 목록과 주문가능 판정을 합쳐 만든다.
 *
 * <p><b>챕터 10</b>에서 신설. 배정 목록은 표시 순서를 위해 읽기 포트에서, 주문가능 여부는 도메인
 * 서비스({@code ShopOperatingStatusService#findOrderMethodAvailabilities})의 시각 의존 판정에서 오므로
 * 두 출처를 합치는 지점이 서비스다. 불가 사유는 <b>주문 가능하면 두 필드 모두 null</b>이라는 판정이
 * 붙은 강등값이다.
 */
public record ShopOrderMethodItemResult(
    String code,
    String name,
    boolean orderable,
    String unavailableReason,
    String unavailableReasonName
) {
}
