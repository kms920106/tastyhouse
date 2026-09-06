package com.tastyhouse.infrastructure.shop.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_CONVENIENCE_INFO")
public class ShopConvenienceInfoJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "is_parking_available", nullable = false)
    private boolean parkingAvailable;

    @Column(name = "is_parking_paid", nullable = false)
    private boolean parkingPaid;

    @Column(name = "is_valet_available", nullable = false)
    private boolean valetAvailable;

    @Column(name = "is_valet_paid", nullable = false)
    private boolean valetPaid;

    @Column(name = "directions_guide", length = 200)
    private String directionsGuide;

    @Column(name = "display_latitude", precision = 9, scale = 6)
    private BigDecimal displayLatitude;

    @Column(name = "display_longitude", precision = 9, scale = 6)
    private BigDecimal displayLongitude;

    protected ShopConvenienceInfoJpaEntity() {
    }

    private ShopConvenienceInfoJpaEntity(
        Long shopId,
        boolean parkingAvailable,
        boolean parkingPaid,
        boolean valetAvailable,
        boolean valetPaid,
        String directionsGuide,
        BigDecimal displayLatitude,
        BigDecimal displayLongitude
    ) {
        this.shopId = shopId;
        this.parkingAvailable = parkingAvailable;
        this.parkingPaid = parkingPaid;
        this.valetAvailable = valetAvailable;
        this.valetPaid = valetPaid;
        this.directionsGuide = directionsGuide;
        this.displayLatitude = displayLatitude;
        this.displayLongitude = displayLongitude;
    }

    static ShopConvenienceInfoJpaEntity create(
        Long shopId,
        boolean parkingAvailable,
        boolean parkingPaid,
        boolean valetAvailable,
        boolean valetPaid,
        String directionsGuide,
        BigDecimal displayLatitude,
        BigDecimal displayLongitude
    ) {
        return new ShopConvenienceInfoJpaEntity(shopId, parkingAvailable, parkingPaid, valetAvailable, valetPaid,
            directionsGuide, displayLatitude, displayLongitude);
    }

    void applyChanges(
        boolean parkingAvailable,
        boolean parkingPaid,
        boolean valetAvailable,
        boolean valetPaid,
        String directionsGuide,
        BigDecimal displayLatitude,
        BigDecimal displayLongitude
    ) {
        this.parkingAvailable = parkingAvailable;
        this.parkingPaid = parkingPaid;
        this.valetAvailable = valetAvailable;
        this.valetPaid = valetPaid;
        this.directionsGuide = directionsGuide;
        this.displayLatitude = displayLatitude;
        this.displayLongitude = displayLongitude;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
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
}
