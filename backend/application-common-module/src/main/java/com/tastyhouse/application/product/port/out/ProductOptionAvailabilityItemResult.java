package com.tastyhouse.application.product.port.out;

import java.time.LocalDateTime;

/**
 * 품절·숨김 관리 화면의 옵션 탭 개별 옵션 항목 read model. {@code optionType}("NORMAL"|"COMMON")으로
 * 일반/공통 옵션 갈래를 구분한다 — 두 테이블의 id 공간이 서로 독립적이라 값 자체만으로는 구분되지 않는다.
 */
public record ProductOptionAvailabilityItemResult(
    Long id,
    String optionType,
    String name,
    Integer additionalPrice,
    boolean soldOut,
    LocalDateTime soldOutUntil,
    boolean visible,
    Integer sort
) {
}
