package com.tastyhouse.domain.shop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopRiderGuide {
    private static final int VISIT_GUIDE_MAX_LENGTH = 200;
    private static final int PICKUP_DETAIL_ADDRESS_MAX_LENGTH = 100;
    private static final BigDecimal LATITUDE_MIN = BigDecimal.valueOf(-90);
    private static final BigDecimal LATITUDE_MAX = BigDecimal.valueOf(90);
    private static final BigDecimal LONGITUDE_MIN = BigDecimal.valueOf(-180);
    private static final BigDecimal LONGITUDE_MAX = BigDecimal.valueOf(180);

    private final Long id;
    private final ShopId shopId;
    private String visitGuide;
    private String pickupRoadAddress;
    private String pickupLotAddress;
    private String pickupDetailAddress;
    private BigDecimal pickupLatitude;
    private BigDecimal pickupLongitude;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

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

    public static ShopRiderGuide of(ShopId shopId) {
        return new ShopRiderGuide(null, shopId, null, null, null, null, null, null, null, null);
    }

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

    public void changeVisitGuide(String visitGuide) {
        String normalized = normalize(visitGuide);
        validateVisitGuide(normalized);

        this.visitGuide = normalized;
    }

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

    public void clearPickupLocation() {
        this.pickupRoadAddress = null;
        this.pickupLotAddress = null;
        this.pickupDetailAddress = null;
        this.pickupLatitude = null;
        this.pickupLongitude = null;
    }

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
