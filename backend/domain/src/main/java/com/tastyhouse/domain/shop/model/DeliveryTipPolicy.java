package com.tastyhouse.domain.shop.model;

import java.util.Set;

/**
 * 배달팁 공용 정책 상수({@code SlotPolicy} 선례의 상수 전용 final class).
 *
 * <p>구간 개수·금액 상한·기본배달거리 허용값처럼 <b>여러 애그리거트와 서비스가 함께 참조하는</b> 값만
 * 여기에 모은다. 특정 애그리거트 하나만 쓰는 값은 그 애그리거트에 둔다.
 */
public final class DeliveryTipPolicy {

    /** 기본(구간별) 배달팁 최대 구간 수 — 기본 1 + 추가 2. */
    public static final int TIER_MAX_COUNT = 3;

    /** 구간별 배달팁 상한(미포함) — 5,000원 자체는 입력할 수 없다. */
    public static final int TIER_TIP_UPPER_BOUND_EXCLUSIVE = 5000;

    /** 추가 배달팁(거리별·지역별·시간별·공휴일) 상한(포함). */
    public static final int EXTRA_TIP_UPPER_BOUND = 10000;

    /** 기본배달거리 허용값(m) — 1 / 1.5 / 2 / 2.5 / 3km. */
    public static final Set<Integer> BASE_DISTANCE_OPTIONS = Set.of(1000, 1500, 2000, 2500, 3000);

    private DeliveryTipPolicy() {
    }
}
