package com.tastyhouse.domain.shop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopConvenienceInfo {
    private static final int DIRECTIONS_GUIDE_MAX_LENGTH = 200;

    private final Long id;
    private final ShopId shopId;
    private boolean parkingAvailable;
    private boolean parkingPaid;
    private boolean valetAvailable;
    private boolean valetPaid;
    private String directionsGuide;
    private BigDecimal displayLatitude;
    private BigDecimal displayLongitude;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ShopConvenienceInfo(
        Long id,
        ShopId shopId,
        boolean parkingAvailable,
        boolean parkingPaid,
        boolean valetAvailable,
        boolean valetPaid,
        String directionsGuide,
        BigDecimal displayLatitude,
        BigDecimal displayLongitude,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.parkingAvailable = parkingAvailable;
        this.parkingPaid = parkingPaid;
        this.valetAvailable = valetAvailable;
        this.valetPaid = valetPaid;
        this.directionsGuide = directionsGuide;
        this.displayLatitude = displayLatitude;
        this.displayLongitude = displayLongitude;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ShopConvenienceInfo of(
        ShopId shopId,
        boolean parkingAvailable,
        boolean parkingPaid,
        boolean valetAvailable,
        boolean valetPaid,
        String directionsGuide,
        BigDecimal displayLatitude,
        BigDecimal displayLongitude
    ) {
        validateDirectionsGuide(directionsGuide);

        return new ShopConvenienceInfo(null, shopId, parkingAvailable, parkingPaid, valetAvailable, valetPaid,
            directionsGuide, displayLatitude, displayLongitude, null, null);
    }

    public static ShopConvenienceInfo reconstitute(
        Long id,
        ShopId shopId,
        boolean parkingAvailable,
        boolean parkingPaid,
        boolean valetAvailable,
        boolean valetPaid,
        String directionsGuide,
        BigDecimal displayLatitude,
        BigDecimal displayLongitude,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopConvenienceInfo(id, shopId, parkingAvailable, parkingPaid, valetAvailable, valetPaid,
            directionsGuide, displayLatitude, displayLongitude, createdAt, updatedAt);
    }

    public void update(
        boolean parkingAvailable,
        boolean parkingPaid,
        boolean valetAvailable,
        boolean valetPaid,
        String directionsGuide,
        BigDecimal displayLatitude,
        BigDecimal displayLongitude
    ) {
        validateDirectionsGuide(directionsGuide);

        this.parkingAvailable = parkingAvailable;
        this.parkingPaid = parkingPaid;
        this.valetAvailable = valetAvailable;
        this.valetPaid = valetPaid;
        this.directionsGuide = directionsGuide;
        this.displayLatitude = displayLatitude;
        this.displayLongitude = displayLongitude;
    }

    private static void validateDirectionsGuide(String directionsGuide) {
        if (directionsGuide != null && directionsGuide.length() > DIRECTIONS_GUIDE_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.SHOP_DIRECTIONS_GUIDE_TOO_LONG);
        }
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public boolean isParkingAvailable() {
        return this.parkingAvailable;
    }

    public boolean isParkingPaid() {
        return this.parkingPaid;
    }

    public boolean isValetAvailable() {
        return this.valetAvailable;
    }

    public boolean isValetPaid() {
        return this.valetPaid;
    }

    public String getDirectionsGuide() {
        return this.directionsGuide;
    }

    public BigDecimal getDisplayLatitude() {
        return this.displayLatitude;
    }

    public BigDecimal getDisplayLongitude() {
        return this.displayLongitude;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
