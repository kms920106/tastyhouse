package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_CHANGE_HISTORY")
public class ShopChangeHistoryJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "category", nullable = false, length = 40, columnDefinition = "VARCHAR(40)")
    private String category;

    @Column(name = "change_type", nullable = false, length = 40, columnDefinition = "VARCHAR(40)")
    private String changeType;

    @Column(name = "action_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String actionType;

    @Column(name = "actor_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String actorType;

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
        String category,
        String changeType,
        String actionType,
        String actorType,
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
        String category,
        String changeType,
        String actionType,
        String actorType,
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

    public String getCategory() {
        return this.category;
    }

    public String getChangeType() {
        return this.changeType;
    }

    public String getActionType() {
        return this.actionType;
    }

    public String getActorType() {
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
