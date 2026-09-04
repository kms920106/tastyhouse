package com.tastyhouse.application.shop.port.out;

import java.util.List;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.OrderUnavailableReason;

/**
 * 가게 주문가능 상태 조회 결과 — 가게 전체 상태와 배정된 주문유형별 상태.
 *
 * <p><b>챕터 09</b>에서 신설. 판정은 도메인 서비스({@code ShopOperatingStatusService})가 수행하고 그
 * 결과가 {@code Map<OrderMethod, ShopOperatingStatusResult>}로 나오는데, <b>맵의 키가 도메인 enum</b>이라
 * 표현 계층이 순회 순서를 정하는 형태가 된다. 유형별 항목을 순서가 정해진 {@code List}로 펼쳐
 * application이 넘긴다.
 *
 * <p>enum → 문자열 강등({@code name()}·{@code getDisplayName()})은 표현 계약이 수행한다 — 챕터 07이
 * 정한 정상 경로이며, 그래서 이 record는 도메인 enum을 그대로 들고 있다.
 */
public record ShopOrderAvailabilityViewResult(
    boolean orderable,
    OrderUnavailableReason unavailableReason,
    List<OrderMethodAvailability> orderMethods
) {

    /** 주문유형 하나의 주문가능 상태. */
    public record OrderMethodAvailability(
        OrderMethod orderMethod,
        boolean orderable,
        OrderUnavailableReason unavailableReason
    ) {
    }
}
