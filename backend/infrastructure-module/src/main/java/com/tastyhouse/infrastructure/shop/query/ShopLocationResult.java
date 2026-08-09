package com.tastyhouse.infrastructure.shop.query;

import java.math.BigDecimal;

/**
 * 가게 좌표 한 건(배달지역 미리보기의 기준점).
 *
 * <p>조회 전용 경로가 소유권 검증을 위해 애그리거트를 통째로 로드하지 않도록, 필요한 좌표만 투영한다 —
 * 소유권은 조회 조건({@code ceo_id})으로 함께 강제하므로 별도 검증기(write 포트를 보유한다)를 주입하지
 * 않아도 된다.
 */
public record ShopLocationResult(
    long shopId,
    BigDecimal latitude,
    BigDecimal longitude
) {
}
