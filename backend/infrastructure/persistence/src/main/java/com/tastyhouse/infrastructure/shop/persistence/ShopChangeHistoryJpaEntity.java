package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActorType;
import com.tastyhouse.domain.shop.model.ShopChangeCategory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_CHANGE_HISTORY")
public class ShopChangeHistoryJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 40, columnDefinition = "VARCHAR(40)")
    private ShopChangeCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 40, columnDefinition = "VARCHAR(40)")
    private ShopChangeType changeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ShopChangeActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ShopChangeActorType actorType;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(name = "previous_value", columnDefinition = "TEXT")
    private String previousValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    protected ShopChangeHistoryJpaEntity() {
    }

    private ShopChangeHistoryJpaEntity(
        Long shopId,
        ShopChangeCategory category,
        ShopChangeType changeType,
        ShopChangeActionType actionType,
        ShopChangeActorType actorType,
        Long actorId,
        String previousValue,
        String newValue
    ) {
        this.shopId = shopId;
        this.category = category;
        this.changeType = changeType;
        this.actionType = actionType;
        this.actorType = actorType;
        this.actorId = actorId;
        this.previousValue = previousValue;
        this.newValue = newValue;
    }

    static ShopChangeHistoryJpaEntity create(
        Long shopId,
        ShopChangeCategory category,
        ShopChangeType changeType,
        ShopChangeActionType actionType,
        ShopChangeActorType actorType,
        Long actorId,
        String previousValue,
        String newValue
    ) {
        return new ShopChangeHistoryJpaEntity(shopId, category, changeType, actionType, actorType, actorId,
            previousValue, newValue);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public ShopChangeCategory getCategory() {
        return this.category;
    }

    public ShopChangeType getChangeType() {
        return this.changeType;
    }

    public ShopChangeActionType getActionType() {
        return this.actionType;
    }

    public ShopChangeActorType getActorType() {
        return this.actorType;
    }

    public Long getActorId() {
        return this.actorId;
    }

    public String getPreviousValue() {
        return this.previousValue;
    }

    public String getNewValue() {
        return this.newValue;
    }
}
