package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_OWNER_MESSAGE_HISTORY")
public class ShopOwnerMessageHistoryJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    protected ShopOwnerMessageHistoryJpaEntity() {
    }

    private ShopOwnerMessageHistoryJpaEntity(Long shopId, String message) {
        this.shopId = shopId;
        this.message = message;
    }

    static ShopOwnerMessageHistoryJpaEntity create(Long shopId, String message) {
        return new ShopOwnerMessageHistoryJpaEntity(shopId, message);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public String getMessage() {
        return this.message;
    }
}
