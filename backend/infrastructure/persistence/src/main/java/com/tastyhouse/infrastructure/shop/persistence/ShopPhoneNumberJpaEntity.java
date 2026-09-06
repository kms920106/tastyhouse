package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_PHONE_NUMBER")
public class ShopPhoneNumberJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "is_virtual", nullable = false)
    private boolean virtual;

    protected ShopPhoneNumberJpaEntity() {
    }

    private ShopPhoneNumberJpaEntity(Long shopId, String phoneNumber, boolean primary, boolean virtual) {
        this.shopId = shopId;
        this.phoneNumber = phoneNumber;
        this.primary = primary;
        this.virtual = virtual;
    }

    static ShopPhoneNumberJpaEntity create(Long shopId, String phoneNumber, boolean primary, boolean virtual) {
        return new ShopPhoneNumberJpaEntity(shopId, phoneNumber, primary, virtual);
    }

    void applyChanges(boolean primary) {
        this.primary = primary;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public boolean isPrimary() {
        return this.primary;
    }

    public boolean isVirtual() {
        return this.virtual;
    }
}
