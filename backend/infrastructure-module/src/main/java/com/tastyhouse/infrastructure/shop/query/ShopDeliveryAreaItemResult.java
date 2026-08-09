package com.tastyhouse.infrastructure.shop.query;

/**
 * 가게 배달가능지역 한 건(점주 설정 화면 목록용).
 *
 * <p>{@code regionName}은 {@code "서울특별시 강남구 역삼1동"} 형태로 <b>DAO가 ADMIN_DONG을 조인해 완성</b>한
 * 표시용 이름이다 — 파일 URL 조립과 같은 근거로, 표현용 값의 완성은 read 측 책임이며 프론트가 세 조각을
 * 받아 조립하지 않는다.
 *
 * <p>{@code source}({@code MANUAL}/{@code POLYGON})는 화면이 "지도로 그린 영역에서 자동 포함된 동"과
 * "직접 추가한 동"을 구분해 보여주기 위한 값이다. HTTP 경계는 도메인 enum을 노출하지 않으므로 {@code String}
 * 으로 투영한다(도메인 enum 경계 규칙).
 */
public record ShopDeliveryAreaItemResult(
    long id,
    long adminDongId,
    String regionName,
    String source
) {
}
