package com.tastyhouse.infrastructure.shop.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(
    name = "SHOP_DELIVERY_AREA_POLYGON",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_shop_delivery_area_polygon_shop_id",
        columnNames = "shop_id"
    )
)
public class ShopDeliveryAreaPolygonJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "rings", nullable = false, columnDefinition = "LONGTEXT")
    private String rings;

    @Column(name = "center_latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal centerLatitude;

    @Column(name = "center_longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal centerLongitude;

    @Column(name = "max_radius_meters", nullable = false)
    private Integer maxRadiusMeters;

    @SuppressWarnings("unused")
    @Column(name = "ring_count", nullable = false)
    private Integer ringCount;

    @SuppressWarnings("unused")
    @Column(name = "vertex_count", nullable = false)
    private Integer vertexCount;

    protected ShopDeliveryAreaPolygonJpaEntity() {
    }

    private ShopDeliveryAreaPolygonJpaEntity(
        Long shopId,
        String rings,
        BigDecimal centerLatitude,
        BigDecimal centerLongitude,
        Integer maxRadiusMeters,
        Integer ringCount,
        Integer vertexCount
    ) {
        this.shopId = shopId;
        this.rings = rings;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        this.maxRadiusMeters = maxRadiusMeters;
        this.ringCount = ringCount;
        this.vertexCount = vertexCount;
    }

    static ShopDeliveryAreaPolygonJpaEntity create(
        Long shopId,
        String rings,
        BigDecimal centerLatitude,
        BigDecimal centerLongitude,
        Integer maxRadiusMeters,
        Integer ringCount,
        Integer vertexCount
    ) {
        return new ShopDeliveryAreaPolygonJpaEntity(
            shopId,
            rings,
            centerLatitude,
            centerLongitude,
            maxRadiusMeters,
            ringCount,
            vertexCount
        );
    }

    void applyChanges(
        String rings,
        BigDecimal centerLatitude,
        BigDecimal centerLongitude,
        Integer maxRadiusMeters,
        Integer ringCount,
        Integer vertexCount
    ) {
        this.rings = rings;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        this.maxRadiusMeters = maxRadiusMeters;
        this.ringCount = ringCount;
        this.vertexCount = vertexCount;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public String getRings() {
        return this.rings;
    }

    public BigDecimal getCenterLatitude() {
        return this.centerLatitude;
    }

    public BigDecimal getCenterLongitude() {
        return this.centerLongitude;
    }

    public Integer getMaxRadiusMeters() {
        return this.maxRadiusMeters;
    }
}
