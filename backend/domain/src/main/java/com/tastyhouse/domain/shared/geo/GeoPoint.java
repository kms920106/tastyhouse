package com.tastyhouse.domain.shared.geo;

import java.math.BigDecimal;

/**
 * 위경도 한 점.
 *
 * <p>{@link GeoDistance}가 이미 {@code (lat, lon)} 네 개의 {@code BigDecimal}을 낱개로 받고 있었는데,
 * 배달지역 도형은 점을 <b>수천 개</b> 다루므로 낱개 전달로는 좌표쌍이 흐트러진다. 점을 타입으로 묶어
 * 위경도 뒤바뀜을 컴파일 단계에서 막는다.
 *
 * <p><b>컴포넌트 선언 순서는 알파벳순({@code latitude} → {@code longitude})이다.</b> 두 컴포넌트가 같은
 * {@code BigDecimal} 타입이라 순서가 어긋나면 컴파일은 통과하고 <b>값만 조용히 뒤바뀐다</b>. 리포 규약
 * ({@code EmbeddedRecordComponentOrderTest})이 같은 사고를 막기 위해 세운 규칙을 그대로 따른다.
 *
 * <p>저장 정밀도가 {@code DECIMAL(9,6)}(≈11cm)이므로 소수점 6자리를 넘는 입력은 영속 단계에서 잘린다.
 */
public record GeoPoint(
    BigDecimal latitude,
    BigDecimal longitude
) {

    /** 위도 하한(도). */
    public static final BigDecimal MIN_LATITUDE = BigDecimal.valueOf(-90);

    /** 위도 상한(도). */
    public static final BigDecimal MAX_LATITUDE = BigDecimal.valueOf(90);

    /** 경도 하한(도). */
    public static final BigDecimal MIN_LONGITUDE = BigDecimal.valueOf(-180);

    /** 경도 상한(도). */
    public static final BigDecimal MAX_LONGITUDE = BigDecimal.valueOf(180);

    public GeoPoint {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("좌표의 위도·경도는 필수입니다.");
        }
        if (latitude.compareTo(MIN_LATITUDE) < 0 || latitude.compareTo(MAX_LATITUDE) > 0) {
            throw new IllegalArgumentException("위도는 -90 이상 90 이하여야 합니다: " + latitude);
        }
        if (longitude.compareTo(MIN_LONGITUDE) < 0 || longitude.compareTo(MAX_LONGITUDE) > 0) {
            throw new IllegalArgumentException("경도는 -180 이상 180 이하여야 합니다: " + longitude);
        }
    }

    public static GeoPoint of(BigDecimal latitude, BigDecimal longitude) {
        return new GeoPoint(latitude, longitude);
    }

    public static GeoPoint of(double latitude, double longitude) {
        return new GeoPoint(BigDecimal.valueOf(latitude), BigDecimal.valueOf(longitude));
    }

    /** 다른 점까지의 하버사인 직선거리(m). */
    public double distanceMetersTo(GeoPoint other) {
        return GeoDistance.distanceMeters(this.latitude, this.longitude, other.latitude, other.longitude);
    }

    /**
     * 두 점이 <b>좌표값으로</b> 같은지. {@code BigDecimal}의 {@code equals}는 스케일까지 비교해
     * {@code 37.5}와 {@code 37.500000}을 다르게 보므로, 연속 중복점 제거에는 이 메서드를 쓴다.
     */
    public boolean isSameLocation(GeoPoint other) {
        return other != null
            && this.latitude.compareTo(other.latitude) == 0
            && this.longitude.compareTo(other.longitude) == 0;
    }
}
