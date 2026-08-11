package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.repository.ShopChangeHistoryRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 가게 변경이력 기록을 소유하는 도메인 서비스.
 *
 * <p>변경을 실제 수행하는 지점(대부분 domain-module 도메인 서비스)이 직접 호출한다. 도메인 서비스는
 * 불변식 검증을 위해 이미 애그리거트를 로드해 둔 상태라 <b>추가 조회 없이</b> 변경 전 값을 얻는 반면,
 * ceo-api의 {@code CommandService}는 CQRS 교차 주입 금지로 QueryDao를 주입할 수 없어 변경 전 값을
 * 구조적으로 볼 수 없다.
 *
 * <p>AOP나 도메인 이벤트를 쓰지 않는 이유: 이 저장소의 이벤트 리스너는 전부
 * {@code @TransactionalEventListener(AFTER_COMMIT)}이라 이력이 변경과 원자적이지 않다. 리스너가 실패하면
 * 변경만 남고 이력이 유실되는데, 이력은 "그때 무엇을 바꿨는가"의 유일한 근거라서 조용한 유실이 곧
 * 기능 상실이다. 같은 트랜잭션에서 동기 기록한다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code DomainServiceConfig}가 담당한다.
 */
public class ShopChangeHistoryRecorder {

    private final ShopChangeHistoryRepository shopChangeHistoryRepository;

    public ShopChangeHistoryRecorder(ShopChangeHistoryRepository shopChangeHistoryRepository) {
        this.shopChangeHistoryRepository = shopChangeHistoryRepository;
    }

    /**
     * 변경이력 1행을 기록한다.
     *
     * @param previousValue 변경 전 요약. 등록({@code CREATE})이면 null
     * @param newValue 변경 후 요약. 삭제({@code DELETE})면 null
     */
    public void record(
        ShopId shopId,
        ShopChangeType changeType,
        ShopChangeActionType actionType,
        ShopChangeActor actor,
        String previousValue,
        String newValue
    ) {
        ShopChangeHistory history = ShopChangeHistory.of(
            shopId,
            changeType,
            actionType,
            actor,
            previousValue,
            newValue
        );
        shopChangeHistoryRepository.save(history);
    }
}
