package com.tastyhouse.application.product.port.out;

import java.time.LocalDateTime;

/**
 * 품절·숨김 관리 화면의 메뉴 탭 항목 read model. 카테고리 그룹핑 필드를 앞에 두어 소비 측이 자연스럽게
 * 카테고리별로 묶을 수 있게 한다. 손님 화면과 달리 품절·숨김 상품도 포함해 조회한다.
 */
public record ProductAvailabilityItemResult(
    Long categoryId,
    String categoryName,
    Integer categorySort,
    Long id,
    String name,
    Integer originalPrice,
    Integer discountPrice,
    String imageUrl,
    boolean soldOut,
    LocalDateTime soldOutUntil,
    boolean visible,
    boolean representative,
    Integer sort
) {
}
