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
@Table(name = "SHOP_RIDER_GUIDE")
public class ShopRiderGuideJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "visit_guide", length = 200)
    private String visitGuide;

    @Column(name = "pickup_road_address")
    private String pickupRoadAddress;

    @Column(name = "pickup_lot_address")
    private String pickupLotAddress;

    @Column(name = "pickup_detail_address", length = 100)
    private String pickupDetailAddress;

    @Column(name = "pickup_latitude", precision = 11, scale = 8)
    private BigDecimal pickupLatitude;

    @Column(name = "pickup_longitude", precision = 11, scale = 8)
    private BigDecimal pickupLongitude;

    protected ShopRiderGuideJpaEntity() {
    }

    private ShopRiderGuideJpaEntity(
        Long shopId,
        String visitGuide,
        String pickupRoadAddress,
        String pickupLotAddress,
        String pickupDetailAddress,
        BigDecimal pickupLatitude,
        BigDecimal pickupLongitude
    ) {
        this.shopId = shopId;
        this.visitGuide = visitGuide;
        this.pickupRoadAddress = pickupRoadAddress;
        this.pickupLotAddress = pickupLotAddress;
        this.pickupDetailAddress = pickupDetailAddress;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
    }

    static ShopRiderGuideJpaEntity create(
        Long shopId,
        String visitGuide,
        String pickupRoadAddress,
        String pickupLotAddress,
        String pickupDetailAddress,
        BigDecimal pickupLatitude,
        BigDecimal pickupLongitude
    ) {
        return new ShopRiderGuideJpaEntity(shopId, visitGuide, pickupRoadAddress, pickupLotAddress,
            pickupDetailAddress, pickupLatitude, pickupLongitude);
    }

    void applyChanges(
        String visitGuide,
        String pickupRoadAddress,
        String pickupLotAddress,
        String pickupDetailAddress,
        BigDecimal pickupLatitude,
        BigDecimal pickupLongitude
    ) {
        this.visitGuide = visitGuide;
        this.pickupRoadAddress = pickupRoadAddress;
        this.pickupLotAddress = pickupLotAddress;
        this.pickupDetailAddress = pickupDetailAddress;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
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
}
