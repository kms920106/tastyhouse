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

/**
 * 가게 변경이력 JPA 영속 모델(append-only). 순수 도메인 모델 {@code ShopChangeHistory}와 분리된 영속 전용
 * 엔티티다.
 *
 * <p>enum 필드는 {@code @Enumerated(EnumType.STRING)}과 {@code columnDefinition = "VARCHAR(n)"}을
 * 병기한다 — {@code columnDefinition}을 빠뜨리면 Hibernate 6의 {@code MySQLDialect}가 네이티브
 * {@code ENUM(...)}을 기대해 {@code ddl-auto=validate}에서 부팅이 실패한다. {@code n}은 {@code schema.sql}과
 * 일치해야 한다(category/changeType 40, actionType/actorType 20).
 */
@Entity
@Table(name = "SHOP_CHANGE_HISTORY")
public class ShopChangeHistoryJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 40, columnDefinition = "VARCHAR(40)")
    private ShopChangeCategory category; // 대분류 (OPERATION, DELIVERY, SHOP_INFO, IMAGE, RIDER)

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 40, columnDefinition = "VARCHAR(40)")
    private ShopChangeType changeType; // 중분류 (BUSINESS_HOUR 등 29종)

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ShopChangeActionType actionType; // 조치 유형 (CREATE, UPDATE, DELETE)

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ShopChangeActorType actorType; // 변경 주체 (CEO, ADMIN)

    @Column(name = "actor_id", nullable = false)
    private Long actorId; // 변경 주체 ID (CEO.id 또는 ADMIN.id 참조)

    @Column(name = "previous_value", columnDefinition = "TEXT")
    private String previousValue; // 변경 전 요약 (등록 시 NULL)

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue; // 변경 후 요약 (삭제 시 NULL)

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
