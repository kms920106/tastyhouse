package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentActionType;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;
import com.tastyhouse.domain.shop.repository.ShopCeoAssignmentHistoryRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 가게-점주 접근권한 이력 기록을 소유하는 도메인 서비스.
 *
 * <p>배정·해제를 실제 수행하는 도메인 서비스({@code ShopCeoAssignmentService}·
 * {@code ShopLifecycleService})가 같은 트랜잭션에서 직접 호출한다.
 *
 * <p>AOP나 도메인 이벤트를 쓰지 않는 이유는 {@code ShopChangeHistoryRecorder}와 같다 — 이 저장소의
 * 이벤트 리스너는 전부 {@code @TransactionalEventListener(AFTER_COMMIT)}이라 이력이 변경과 원자적이지
 * 않다. 리스너가 실패하면 권한만 바뀌고 이력이 유실되는데, 이 이력은 "언제부터 언제까지 그 점주가 이
 * 가게의 개인정보에 접근할 수 있었는가"의 유일한 근거라서 조용한 유실이 곧 기능 상실이다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code DomainServiceConfig}가 담당한다.
 */
public class ShopCeoAssignmentRecorder {

    private final ShopCeoAssignmentHistoryRepository shopCeoAssignmentHistoryRepository;

    public ShopCeoAssignmentRecorder(
        ShopCeoAssignmentHistoryRepository shopCeoAssignmentHistoryRepository
    ) {
        this.shopCeoAssignmentHistoryRepository = shopCeoAssignmentHistoryRepository;
    }

    /**
     * 접근권한 부여 이력 1행을 기록한다.
     */
    public void recordGrant(ShopId shopId, CeoId ceoId, Long actorAdminId) {
        record(shopId, ceoId, ShopCeoAssignmentActionType.GRANT, actorAdminId);
    }

    /**
     * 접근권한 말소 이력 1행을 기록한다.
     */
    public void recordRevoke(ShopId shopId, CeoId ceoId, Long actorAdminId) {
        record(shopId, ceoId, ShopCeoAssignmentActionType.REVOKE, actorAdminId);
    }

    private void record(
        ShopId shopId,
        CeoId ceoId,
        ShopCeoAssignmentActionType actionType,
        Long actorAdminId
    ) {
        ShopCeoAssignmentHistory history = ShopCeoAssignmentHistory.of(
            shopId,
            ceoId,
            actionType,
            actorAdminId
        );
        shopCeoAssignmentHistoryRepository.save(history);
    }
}
