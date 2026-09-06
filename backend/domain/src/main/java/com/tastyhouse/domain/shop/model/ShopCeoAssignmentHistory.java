package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 가게-점주 접근권한 부여/말소 이력 순수 도메인 모델(append-only).
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopCeoAssignmentHistoryJpaEntity} + {@code ShopCeoAssignmentHistoryMapper}가 담당한다.
 * 기록 후에는 바뀌지 않으므로 전 필드가 final이고 상태전이 메서드가 없다.
 *
 * <p>점주 계정과 가게가 연결된 시점이 곧 개인정보처리시스템 접근권한 부여 시점이고, 해제된 시점이 말소
 * 시점이다. 배정 이후 그 점주는 해당 가게의 주문·회원 정보를 열람할 수 있게 된다.
 *
 * <p>{@code actorType}을 두지 않는 이유: 배정·해제는 관리자만 할 수 있어(점주에게는 권한 등급 개념
 * 자체가 없다 — {@code docs/domain/ceo.md}) 항상 같은 값이 들어가는 컬럼이 되기 때문이다. 그래서
 * {@code actorAdminId} 하나만 둔다.
 */
public class ShopCeoAssignmentHistory {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId;
    private final CeoId ceoId; // 권한이 부여·말소된 대상 점주
    private final ShopCeoAssignmentActionType actionType;
    private final Long actorAdminId; // 조치한 관리자 (ADMIN.id)
    private final LocalDateTime createdAt; // = 조치 시각. 재구성 전 신규 상태는 null

    private ShopCeoAssignmentHistory(
        Long id,
        ShopId shopId,
        CeoId ceoId,
        ShopCeoAssignmentActionType actionType,
        Long actorAdminId,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.ceoId = ceoId;
        this.actionType = actionType;
        this.actorAdminId = actorAdminId;
        this.createdAt = createdAt;
    }

    /**
     * 신규 이력을 만든다.
     */
    public static ShopCeoAssignmentHistory of(
        ShopId shopId,
        CeoId ceoId,
        ShopCeoAssignmentActionType actionType,
        Long actorAdminId
    ) {
        return new ShopCeoAssignmentHistory(null, shopId, ceoId, actionType, actorAdminId, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopCeoAssignmentHistory reconstitute(
        Long id,
        ShopId shopId,
        CeoId ceoId,
        ShopCeoAssignmentActionType actionType,
        Long actorAdminId,
        LocalDateTime createdAt
    ) {
        return new ShopCeoAssignmentHistory(id, shopId, ceoId, actionType, actorAdminId, createdAt);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public CeoId getCeoId() {
        return this.ceoId;
    }

    public ShopCeoAssignmentActionType getActionType() {
        return this.actionType;
    }

    public Long getActorAdminId() {
        return this.actorAdminId;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
