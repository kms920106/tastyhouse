package com.tastyhouse.infrastructure.shop.query;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 저장된 배달지역 도형 한 건(편집 화면용).
 *
 * <p>{@code rings}는 인코딩된 원본 문자열이다 — 좌표 객체 배열로의 변환은 API 경계에서 수행한다.
 *
 * <p>{@code centerLatitude}/{@code centerLongitude}는 <b>저장 시점 가게 좌표의 스냅샷</b>이며 현재 좌표와
 * 다를 수 있다. 두 값의 차이가 곧 "주소 이전으로 배달지역 재설정이 필요한 상태"를 뜻한다.
 */
public record ShopDeliveryAreaPolygonResult(
    long id,
    String rings,
    BigDecimal centerLatitude,
    BigDecimal centerLongitude,
    Integer maxRadiusMeters,
    Integer ringCount,
    Integer vertexCount,
    LocalDateTime updatedAt
) {
}
