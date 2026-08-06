package com.tastyhouse.infrastructure.shop.query;

/**
 * 가게 배달가능지역 한 건(점주 설정 화면 목록용).
 *
 * <p>{@code regionName}은 {@code "서울특별시 강남구 역삼1동"} 형태로 <b>DAO가 ADMIN_DONG을 조인해 완성</b>한
 * 표시용 이름이다 — 파일 URL 조립과 같은 근거로, 표현용 값의 완성은 read 측 책임이며 프론트가 세 조각을
 * 받아 조립하지 않는다.
 */
public record ShopDeliveryAreaItemResult(
    long id,
    long adminDongId,
    String regionName
) {
}
