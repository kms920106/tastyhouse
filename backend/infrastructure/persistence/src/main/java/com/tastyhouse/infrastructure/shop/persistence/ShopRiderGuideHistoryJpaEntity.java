package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shop.model.RiderGuideActionType;
import com.tastyhouse.domain.shop.model.RiderGuideActorType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_RIDER_GUIDE_HISTORY")
public class ShopRiderGuideHistoryJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private RiderGuideActorType actorType;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private RiderGuideActionType actionType;

    @Column(name = "previous_visit_guide", length = 200)
    private String previousVisitGuide;

    @Column(name = "new_visit_guide", length = 200)
    private String newVisitGuide;

    @Column(name = "reason", length = 200)
    private String reason;

    protected ShopRiderGuideHistoryJpaEntity() {
    }

    private ShopRiderGuideHistoryJpaEntity(
        Long shopId,
        RiderGuideActorType actorType,
        Long actorId,
        RiderGuideActionType actionType,
        String previousVisitGuide,
        String newVisitGuide,
        String reason
    ) {
        this.shopId = shopId;
        this.actorType = actorType;
        this.actorId = actorId;
        this.actionType = actionType;
        this.previousVisitGuide = previousVisitGuide;
        this.newVisitGuide = newVisitGuide;
        this.reason = reason;
    }

    static ShopRiderGuideHistoryJpaEntity create(
        Long shopId,
        RiderGuideActorType actorType,
        Long actorId,
        RiderGuideActionType actionType,
        String previousVisitGuide,
        String newVisitGuide,
        String reason
    ) {
        return new ShopRiderGuideHistoryJpaEntity(shopId, actorType, actorId, actionType, previousVisitGuide,
            newVisitGuide, reason);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public RiderGuideActorType getActorType() {
        return this.actorType;
    }

    public Long getActorId() {
        return this.actorId;
    }

    public RiderGuideActionType getActionType() {
        return this.actionType;
    }

    public String getPreviousVisitGuide() {
        return this.previousVisitGuide;
    }

    public String getNewVisitGuide() {
        return this.newVisitGuide;
    }

    public String getReason() {
        return this.reason;
    }
}
