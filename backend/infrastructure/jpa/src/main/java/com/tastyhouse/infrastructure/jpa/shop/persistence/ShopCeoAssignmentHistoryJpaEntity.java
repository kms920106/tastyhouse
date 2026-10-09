package com.tastyhouse.infrastructure.jpa.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.jpa.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_CEO_ASSIGNMENT_HISTORY")
class ShopCeoAssignmentHistoryJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "ceo_id", nullable = false)
    private Long ceoId;

    @Column(name = "action_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String actionType;

    @Column(name = "actor_admin_id", nullable = false)
    private Long actorAdminId;

    protected ShopCeoAssignmentHistoryJpaEntity() {
    }

    private ShopCeoAssignmentHistoryJpaEntity(
        Long shopId,
        Long ceoId,
        String actionType,
        Long actorAdminId
    ) {
        this.shopId = shopId;
        this.ceoId = ceoId;
        this.actionType = actionType;
        this.actorAdminId = actorAdminId;
    }

    static ShopCeoAssignmentHistoryJpaEntity create(
        Long shopId,
        Long ceoId,
        String actionType,
        Long actorAdminId
    ) {
        return new ShopCeoAssignmentHistoryJpaEntity(shopId, ceoId, actionType, actorAdminId);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getCeoId() {
        return this.ceoId;
    }

    public String getActionType() {
        return this.actionType;
    }

    public Long getActorAdminId() {
        return this.actorAdminId;
    }
}
