package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;
import java.util.List;

/**
 * 배달지역 반경 미리보기 결과 — 원 근사 도형과 반경 안에 드는 행정동 목록.
 *
 * <p><b>챕터 09</b>에서 신설. 원 근사와 거리 판정(하버사인)은 도메인 기하 타입으로 수행하므로
 * application에 남고, 좌표는 {@link GeoPointView}로 강등해 나른다.
 */
public record ShopDeliveryAreaRadiusPreviewResult(
    BigDecimal centerLatitude,
    BigDecimal centerLongitude,
    int radiusMeters,
    int maxAllowedRadiusMeters,
    int defaultExposureRadiusMeters,
    List<GeoPointView> circle,
    List<ShopDeliveryAreaCandidateView> adminDongs,
    int adminDongCount,
    int unresolvedCount
) {
}
