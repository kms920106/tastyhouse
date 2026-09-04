package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 저장된 배달지역 도형 조회 결과.
 *
 * <p><b>챕터 09</b>에서 신설. 도형은 인코딩된 문자열로 저장돼 있어 {@code GeoRingsPort}로 디코딩한 뒤
 * 도메인 기하 타입으로 계산하고(중심 이동 거리 등), 그 결과를 좌표 쌍으로 강등해 나른다 — api 모듈은
 * {@code domain.shared.geo..}를 알 수 없다({@link GeoPointView} 참고).
 *
 * <p>{@code exists}가 {@code false}면 도형 없이 행정동만 직접 등록한 가게다. 그 경우에도 404가 아니라
 * 200으로 응답하는 것이 이 API의 계약이므로, 정책 상수와 가게 좌표는 그대로 채워 넘긴다.
 */
public record ShopDeliveryAreaPolygonViewResult(
    boolean exists,
    List<List<GeoPointView>> rings,
    BigDecimal centerLatitude,
    BigDecimal centerLongitude,
    BigDecimal shopLatitude,
    BigDecimal shopLongitude,
    int centerMovedMeters,
    Integer maxRadiusMeters,
    int maxAllowedRadiusMeters,
    int defaultExposureRadiusMeters,
    Integer ringCount,
    Integer vertexCount,
    int projectedAdminDongCount,
    LocalDateTime updatedAt
) {
}
