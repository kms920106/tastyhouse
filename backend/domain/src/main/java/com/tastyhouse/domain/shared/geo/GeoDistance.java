package com.tastyhouse.domain.shared.geo;

import java.math.BigDecimal;

/**
 * 좌표 간 직선거리(하버사인) 계산 유틸.
 *
 * <p>원래 {@code ShopConvenienceInfoService}의 private static 메서드였으나, 배달팁 거리별 할증이
 * 같은 계산을 필요로 하면서 <b>공용 위치로 승격</b>했다. 기존 호출부(편의정보 표시위치 반경 검증)도
 * 이 유틸을 쓰도록 교체했으며 계산식은 그대로라 동작은 변하지 않는다.
 *
 * <p>{@code shared}에 두는 이유는 소비자가 두 컨텍스트(가게 편의정보·배달팁)에 걸쳐 있고, 좌표 거리는
 * 어느 한 애그리거트의 규칙이 아니라 순수 수학이기 때문이다.
 */
public final class GeoDistance {

    /** 지구 반지름(m). 하버사인 공식의 상수. */
    public static final double EARTH_RADIUS_METERS = 6371000;

    private GeoDistance() {
    }

    /**
     * 두 좌표 간의 하버사인(Haversine) 거리를 미터 단위로 계산한다.
     *
     * <p>도로 경로가 아니라 <b>직선거리</b>다 — 배달팁 거리별 할증의 기준이 직선거리이고
     * (배민 가이드 원문), 편의정보 표시위치 반경 검증도 같은 기준을 쓴다.
     */
    public static double distanceMeters(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lon1Rad = Math.toRadians(lon1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double lon2Rad = Math.toRadians(lon2.doubleValue());

        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
            + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }
}
