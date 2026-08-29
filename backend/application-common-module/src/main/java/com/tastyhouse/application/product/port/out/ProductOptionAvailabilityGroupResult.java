package com.tastyhouse.application.product.port.out;

import java.util.List;

/**
 * 품절·숨김 관리 화면의 옵션 탭 옵션 그룹 read model. {@code optionType}("NORMAL"|"COMMON")으로 일반/공통
 * 옵션 그룹 갈래를 구분한다. {@code linkedProductNames}는 이 옵션 그룹이 연결된 메뉴명 목록이다.
 */
public record ProductOptionAvailabilityGroupResult(
    Long optionGroupId,
    String optionType,
    String name,
    boolean required,
    Integer minSelect,
    Integer maxSelect,
    List<String> linkedProductNames,
    Integer sort,
    List<ProductOptionAvailabilityItemResult> options
) {
}
