package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shop.model.ShopCeoAssignmentActionType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 가게-점주 접근권한 이력 JPA 영속 모델(append-only). 순수 도메인 모델
 * {@code ShopCeoAssignmentHistory}와 분리된 영속 전용 엔티티다.
 *
 * <p>enum 필드는 {@code @Enumerated(EnumType.STRING)}과 {@code columnDefinition = "VARCHAR(n)"}을
 * 병기한다 — {@code columnDefinition}을 빠뜨리면 Hibernate 6의 {@code MySQLDialect}가 네이티브
 * {@code ENUM(...)}을 기대해 {@code ddl-auto=validate}에서 부팅이 실패한다. {@code n}은
 * {@code schema.sql}과 일치해야 한다(actionType 20).
 */
@Entity
@Table(name = "SHOP_CEO_ASSIGNMENT_HISTORY")
public class ShopCeoAssignmentHistoryJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Column(name = "ceo_id", nullable = false)
    private Long ceoId; // 대상 점주 ID (CEO.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ShopCeoAssignmentActionType actionType; // 조치 유형 (GRANT, REVOKE)

    @Column(name = "actor_admin_id", nullable = false)
    private Long actorAdminId; // 조치한 관리자 ID (ADMIN.id 참조)

    protected ShopCeoAssignmentHistoryJpaEntity() {
    }

    private ShopCeoAssignmentHistoryJpaEntity(
        Long shopId,
        Long ceoId,
        ShopCeoAssignmentActionType actionType,
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
        ShopCeoAssignmentActionType actionType,
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

    public ShopCeoAssignmentActionType getActionType() {
        return this.actionType;
    }

    public Long getActorAdminId() {
        return this.actorAdminId;
    }
}
