package com.tastyhouse.domain.shop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 라이더 가게방문 안내 문구 + 라이더 픽업 위치 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopRiderGuideJpaEntity} + {@code ShopRiderGuideMapper}가 담당한다. 도메인이
 * 프레임워크-프리이므로 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로
 * {@code ShopRiderGuideRepository#save}를 호출해야 한다. shopId당 1개만 존재한다(upsert).
 *
 * <p><b>{@code ShopConvenienceInfo}와 분리한 이유</b>: 편의정보의 찾아오는길·노출위치는 고객에게
 * 보여주는 값이고, 라이더 안내는 <b>고객 비노출</b>이 계약의 핵심이다. 한 애그리거트에 담으면
 * 응답 조립 시 실수로 web-api 응답에 새어 나가는 것을 구조적으로 막을 수 없다.
 *
 * <p><b>픽업 좌표를 {@code Shop.latitude}/{@code longitude}에 인라인하지 않는 이유</b>: 그 값은
 * 배달가능지역 판정과 배달팁 거리 계산의 기준값이라, 픽업 좌표로 덮으면 배달팁 금액이 조용히 달라진다.
 */
public class ShopRiderGuide {

    private static final int VISIT_GUIDE_MAX_LENGTH = 200;
    private static final int PICKUP_DETAIL_ADDRESS_MAX_LENGTH = 100;
    private static final BigDecimal LATITUDE_MIN = BigDecimal.valueOf(-90);
    private static final BigDecimal LATITUDE_MAX = BigDecimal.valueOf(90);
    private static final BigDecimal LONGITUDE_MIN = BigDecimal.valueOf(-180);
    private static final BigDecimal LONGITUDE_MAX = BigDecimal.valueOf(180);

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId;
    private String visitGuide; // 라이더 가게방문 안내 문구 (상태전이로 재대입됨, nullable, 최대 200자)
    private String pickupRoadAddress; // 픽업 도로명주소 (상태전이로 재대입됨, nullable)
    private String pickupLotAddress; // 픽업 지번주소 (상태전이로 재대입됨, nullable)
    private String pickupDetailAddress; // 픽업 상세주소 (상태전이로 재대입됨, nullable, 최대 100자)
    private BigDecimal pickupLatitude; // 픽업 위도 (상태전이로 재대입됨, nullable)
    private BigDecimal pickupLongitude; // 픽업 경도 (상태전이로 재대입됨, nullable)
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private ShopRiderGuide(
        Long id,
        ShopId shopId,
        String visitGuide,
        String pickupRoadAddress,
        String pickupLotAddress,
        String pickupDetailAddress,
        BigDecimal pickupLatitude,
        BigDecimal pickupLongitude,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.visitGuide = visitGuide;
        this.pickupRoadAddress = pickupRoadAddress;
        this.pickupLotAddress = pickupLotAddress;
        this.pickupDetailAddress = pickupDetailAddress;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 아직 문구·픽업 위치가 없는 빈 라이더 안내를 신규 생성한다. 아직 영속되지 않았으므로
     * 식별자·감사 시각은 없다.
     */
    public static ShopRiderGuide of(ShopId shopId) {
        return new ShopRiderGuide(null, shopId, null, null, null, null, null, null, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static ShopRiderGuide reconstitute(
        Long id,
        ShopId shopId,
        String visitGuide,
        String pickupRoadAddress,
        String pickupLotAddress,
        String pickupDetailAddress,
        BigDecimal pickupLatitude,
        BigDecimal pickupLongitude,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopRiderGuide(id, shopId, visitGuide, pickupRoadAddress, pickupLotAddress, pickupDetailAddress,
            pickupLatitude, pickupLongitude, createdAt, updatedAt);
    }

    /**
     * 안내 문구를 변경한다. null·공백은 {@code null}로 정규화한다 — 삭제 전용 엔드포인트를 따로 두지 않고
     * "빈 값 PUT = 삭제"로 통일하기 위함이며, {@code ""}와 {@code null}이 DB에 섞이면 "등록됨/미등록"
     * 판정이 조회 지점마다 갈리기 때문이다.
     */
    public void changeVisitGuide(String visitGuide) {
        String normalized = normalize(visitGuide);
        validateVisitGuide(normalized);

        this.visitGuide = normalized;
    }

    /**
     * 픽업 위치를 변경한다. 주소와 좌표는 전부 채우거나 전부 비우거나 둘 중 하나여야 한다.
     */
    public void changePickupLocation(
        String roadAddress,
        String lotAddress,
        String detailAddress,
        BigDecimal latitude,
        BigDecimal longitude
    ) {
        String normalizedRoadAddress = normalize(roadAddress);
        String normalizedLotAddress = normalize(lotAddress);
        String normalizedDetailAddress = normalize(detailAddress);

        validatePickupCompleteness(normalizedRoadAddress, latitude, longitude);
        validatePickupDetailAddress(normalizedDetailAddress);
        validatePickupCoordinates(latitude, longitude);

        this.pickupRoadAddress = normalizedRoadAddress;
        this.pickupLotAddress = normalizedLotAddress;
        this.pickupDetailAddress = normalizedDetailAddress;
        this.pickupLatitude = latitude;
        this.pickupLongitude = longitude;
    }

    /**
     * 픽업 위치 전 필드를 비운다. 라이더 앱은 이 상태에서 가게 실주소로 폴백한다.
     */
    public void clearPickupLocation() {
        this.pickupRoadAddress = null;
        this.pickupLotAddress = null;
        this.pickupDetailAddress = null;
        this.pickupLatitude = null;
        this.pickupLongitude = null;
    }

    /**
     * 별도 픽업 위치가 설정되어 있는지 판정한다. 도로명주소와 좌표가 모두 채워져야 설정된 것으로 본다.
     */
    public boolean hasPickupLocation() {
        return this.pickupRoadAddress != null && this.pickupLatitude != null && this.pickupLongitude != null;
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static void validateVisitGuide(String visitGuide) {
        if (visitGuide != null && visitGuide.length() > VISIT_GUIDE_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.SHOP_RIDER_VISIT_GUIDE_TOO_LONG);
        }
    }

    private static void validatePickupDetailAddress(String detailAddress) {
        if (detailAddress != null && detailAddress.length() > PICKUP_DETAIL_ADDRESS_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.SHOP_RIDER_PICKUP_DETAIL_ADDRESS_TOO_LONG);
        }
    }

    /**
     * 픽업 위치는 전부 채우거나 전부 비우거나 둘 중 하나다. 위경도 중 하나만 있거나 좌표 없이 주소만
     * 있으면 라이더 앱이 어디로 이동할지 판정할 수 없어 반쪽짜리 상태를 허용하지 않는다.
     */
    private static void validatePickupCompleteness(String roadAddress, BigDecimal latitude, BigDecimal longitude) {
        boolean allPresent = roadAddress != null && latitude != null && longitude != null;
        boolean allAbsent = roadAddress == null && latitude == null && longitude == null;

        if (!allPresent && !allAbsent) {
            throw new BusinessException(ErrorCode.SHOP_RIDER_PICKUP_LOCATION_INCOMPLETE);
        }
    }

    private static void validatePickupCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return;
        }

        boolean latitudeOutOfRange = latitude.compareTo(LATITUDE_MIN) < 0 || latitude.compareTo(LATITUDE_MAX) > 0;
        boolean longitudeOutOfRange = longitude.compareTo(LONGITUDE_MIN) < 0 || longitude.compareTo(LONGITUDE_MAX) > 0;
        if (latitudeOutOfRange || longitudeOutOfRange) {
            throw new BusinessException(ErrorCode.SHOP_RIDER_PICKUP_LOCATION_INVALID);
        }
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public String getVisitGuide() {
        return this.visitGuide;
    }

    public String getPickupRoadAddress() {
        return this.pickupRoadAddress;
    }

    public String getPickupLotAddress() {
        return this.pickupLotAddress;
    }

    public String getPickupDetailAddress() {
        return this.pickupDetailAddress;
    }

    public BigDecimal getPickupLatitude() {
        return this.pickupLatitude;
    }

    public BigDecimal getPickupLongitude() {
        return this.pickupLongitude;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
