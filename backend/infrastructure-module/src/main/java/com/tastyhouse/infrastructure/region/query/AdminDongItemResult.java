package com.tastyhouse.infrastructure.region.query;

/**
 * 행정동 검색 결과 한 건.
 *
 * <p>{@code regionName}은 {@code "서울특별시 강남구 역삼1동"} 형태로 <b>DAO가 SQL에서 조립</b>한 표시용
 * 이름이다({@code ShopDeliveryAreaQueryDao}와 동일한 방식) — 프론트가 세 조각을 받아 조립하지 않는다.
 */
public record AdminDongItemResult(
    Long id,
    String code,
    String regionName
) {
}
