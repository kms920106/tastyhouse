package com.tastyhouse.domain.shop.domain.service;

import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;
import com.tastyhouse.domain.shop.repository.ShopCeoAssignmentHistoryRepository;

/**
 * 접근권한 이력 write 포트 fake — 저장된 이력을 저장 순서 그대로 모아 두어 테스트가 들여다볼 수 있게
 * 한다(재배정이 {@code REVOKE}→{@code GRANT} 순서인지 검증하려면 순서가 보존돼야 한다).
 *
 * <p>{@code ShopCeoAssignmentRecorder}가 {@code ShopLifecycleService}의 생성자 필수 의존이라 배정과
 * 무관한 테스트도 이 fake를 필요로 하므로, {@link RecordingShopChangeHistoryRepository} 선례대로 이
 * 패키지에 하나만 둔다(domain-module에는 Mockito 의존이 없어 fake를 손으로 만든다).
 */
class RecordingShopCeoAssignmentHistoryRepository implements ShopCeoAssignmentHistoryRepository {

    private final List<ShopCeoAssignmentHistory> saved = new ArrayList<>();

    @Override
    public ShopCeoAssignmentHistory save(ShopCeoAssignmentHistory shopCeoAssignmentHistory) {
        saved.add(shopCeoAssignmentHistory);
        return shopCeoAssignmentHistory;
    }

    /** 저장된 이력 전체(저장 순서 그대로). */
    List<ShopCeoAssignmentHistory> saved() {
        return List.copyOf(saved);
    }
}
