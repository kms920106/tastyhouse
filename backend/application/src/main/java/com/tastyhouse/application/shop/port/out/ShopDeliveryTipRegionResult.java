package com.tastyhouse.application.shop.port.out;

/**
 * 지역별 배달팁 한 행(표현용).
 *
 * <p>{@code regionName}은 DAO가 {@code ADMIN_DONG}을 조인해 {@code "서울특별시 강남구 역삼1동"} 형태로
 * 완성한 값이다 — 프론트가 조립하지 않는다.
 */
public record ShopDeliveryTipRegionResult(Long id, Long adminDongId, String regionName, int tipAmount) {
}
