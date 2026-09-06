package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(
    name = "SHOP_DELIVERY_AREA",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_shop_delivery_area",
        columnNames = {"shop_id", "admin_dong_id"}
    ),
    indexes = @Index(name = "idx_shop_delivery_area_shop_id", columnList = "shop_id")
)
public class ShopDeliveryAreaJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "admin_dong_id", nullable = false)
    private Long adminDongId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private DeliveryAreaSource source;

    protected ShopDeliveryAreaJpaEntity() {
    }

    private ShopDeliveryAreaJpaEntity(Long shopId, Long adminDongId, DeliveryAreaSource source) {
        this.shopId = shopId;
        this.adminDongId = adminDongId;
        this.source = source;
    }

    static ShopDeliveryAreaJpaEntity create(Long shopId, Long adminDongId, DeliveryAreaSource source) {
        return new ShopDeliveryAreaJpaEntity(shopId, adminDongId, source);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getAdminDongId() {
        return this.adminDongId;
    }

    public DeliveryAreaSource getSource() {
        return this.source;
    }
}
