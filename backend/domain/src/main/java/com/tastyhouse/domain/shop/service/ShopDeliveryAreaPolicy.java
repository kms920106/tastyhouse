package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shared.geo.GeoRing;

/**
 * 배달지역 설정의 상한과 검증 규칙(무상태).
 *
 * <p>상한을 도메인 계층 한 곳에 모으는 이유는, 같은 규칙을 검증해야 하는 경로가 여럿이기 때문이다 —
 * 행정동 직접 추가·반경 일괄 적용·도형 저장이 모두 "총 개수 상한"을 지켜야 하고, 도형 저장과 미리보기가
 * 모두 "7km 상한"을 본다. 경로마다 숫자를 적으면 한쪽만 고쳐지는 순간 규칙이 갈린다.
 *
 * <p>배열 길이·반경 범위처럼 <b>요청 형식</b> 차원의 상한은 여기 두지 않고 API 경계의 Bean Validation
 * ({@code @Size}/{@code @Min}/{@code @Max})이 담당한다. 이 클래스는 도메인 불변식만 갖는다.
 */
public final class ShopDeliveryAreaPolicy {

    /** 배달지역 최대 반경(m). 가게 주소를 기준점으로 잰다. */
    public static final int MAX_DELIVERY_RADIUS_METERS = 7000;

    /** 반경 설정의 하한(m). 이보다 좁으면 행정동 하나도 걸리지 않아 설정이 의미를 잃는다. */
    public static final int MIN_DELIVERY_RADIUS_METERS = 500;

    /** 가게배달 기본 노출 반경(m). 표시 전용이며 배달지역 판정에 쓰이지 않는다. */
    public static final int DEFAULT_EXPOSURE_RADIUS_METERS = 4000;

    /** 가게당 등록 가능한 배달가능지역(행정동) 최대 개수. */
    public static final int MAX_DELIVERY_AREA_COUNT = 500;

    /** 도형의 최대 링 개수. */
    public static final int MAX_RINGS = 20;

    /** 도형의 최대 총 정점 수. */
    public static final int MAX_VERTICES = 5000;

    /** 반경 원을 근사할 정다각형의 정점 수. */
    public static final int CIRCLE_SEGMENTS = 72;

    /** 경계 정점 샘플 중 도형 내부에 들어야 하는 최소 비율. */
    public static final double COVERAGE_THRESHOLD = 0.30;

    /** 경계 폴리곤에서 뽑는 최대 샘플 정점 수. */
    public static final int BOUNDARY_SAMPLE_LIMIT = 200;

    private ShopDeliveryAreaPolicy() {
    }

    /**
     * 도형의 크기 상한(링 수·정점 수)을 검증한다.
     *
     * <p>거대 도형을 막는 이유는 저장 용량이 아니라 <b>환산 비용</b>이다 — 환산은 후보 행정동마다 도형
     * 전체를 ray-casting 하므로 정점 수가 후보 수만큼 곱해진다. 상한이 없으면 요청 하나로 트랜잭션을
     * 길게 점유할 수 있다.
     */
    public static void validateShape(GeoPolygon polygon) {
        if (polygon.ringCount() > MAX_RINGS) {
            throw new BusinessException(
                ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID,
                ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID.getDefaultMessage()
                    + ": 링은 최대 " + MAX_RINGS + "개까지 가능합니다."
            );
        }
        if (polygon.vertexCount() > MAX_VERTICES) {
            throw new BusinessException(
                ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID,
                ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID.getDefaultMessage()
                    + ": 정점은 최대 " + MAX_VERTICES + "개까지 가능합니다."
            );
        }
        for (GeoRing ring : polygon.rings()) {
            if (ring.vertexCount() < GeoRing.MIN_POINTS) {
                throw new BusinessException(
                    ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID,
                    ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID.getDefaultMessage()
                        + ": 각 링은 좌표가 " + GeoRing.MIN_POINTS + "개 이상이어야 합니다."
                );
            }
        }
    }

    /**
     * 도형의 <b>모든</b> 정점이 기준점에서 7km 이내인지 검증한다.
     *
     * <p>정점 하나만 넘어도 위반이다 — 평균이나 중심 거리로 판정하면 길게 뻗은 도형이 상한을 우회한다.
     */
    public static void validateWithinMaxRadius(GeoPolygon polygon, GeoPoint center) {
        double maxDistance = polygon.maxDistanceMetersFrom(center);
        if (exceedsMaxRadius(maxDistance)) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_RADIUS_EXCEEDED);
        }
    }

    /** 반경 값이 허용 범위(500m ~ 7km) 안인지 검증한다. */
    public static void validateRadius(int radiusMeters) {
        if (radiusMeters < MIN_DELIVERY_RADIUS_METERS || radiusMeters > MAX_DELIVERY_RADIUS_METERS) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_RADIUS_EXCEEDED);
        }
    }

    /**
     * 반영 후 총 개수가 상한 이내인지 검증한다.
     *
     * <p>"추가되는 개수"가 아니라 <b>반영 후 총계</b>를 보는 이유는, 여러 번 나눠 추가하면 상한을 우회할
     * 수 있기 때문이다.
     */
    public static void validateTotalCount(int totalCountAfterApply) {
        if (totalCountAfterApply > MAX_DELIVERY_AREA_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_COUNT_EXCEEDED);
        }
    }

    /**
     * 거리(m)가 배달지역 상한을 넘는지.
     *
     * <p>{@code 7000.0000001}처럼 부동소수 오차로 아슬아슬하게 넘는 값을 위반으로 보지 않도록 1mm(1e-3m)
     * 허용 오차를 둔다 — 정확히 7000m인 도형은 허용되어야 하는데, 하버사인 계산 결과가 상한과 정확히
     * 같기를 기대할 수는 없다.
     */
    public static boolean exceedsMaxRadius(double distanceMeters) {
        return distanceMeters > MAX_DELIVERY_RADIUS_METERS + 1e-3;
    }
}
