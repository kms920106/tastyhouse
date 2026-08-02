package com.tastyhouse.infrastructure.shop.query;

import java.math.BigDecimal;

import com.querydsl.core.annotations.QueryProjection;

/**
 * 지도 마커 표시용 가게 좌표 결과.
 *
 * <p>과거 web-api는 주변 가게를 도메인 모델({@code Shop}) 목록으로 읽어 좌표·상호명만 꺼내 썼다.
 * 표현에 필요한 네 필드만 투영해 도메인 모델 적재를 없앤다.
 */
public record ShopMapMarkerResult(
    Long id,
    BigDecimal latitude,
    BigDecimal longitude,
    String name
) {

    @QueryProjection
    public ShopMapMarkerResult {
    }
}
