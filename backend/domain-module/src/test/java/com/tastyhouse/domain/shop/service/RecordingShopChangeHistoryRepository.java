package com.tastyhouse.domain.shop.service;

import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.repository.ShopChangeHistoryRepository;

/**
 * 변경이력 write 포트 fake — 저장된 이력을 그대로 모아 두어 테스트가 들여다볼 수 있게 한다.
 *
 * <p>변경이력을 기록하는 도메인 서비스가 배달 분류만 5개(배달팁·배달가능지역·반경·도형·조정신청)로 늘면서
 * 각 테스트가 같은 fake를 복제하고 있었으므로 이 패키지에 하나만 둔다(domain-module에는 Mockito 의존이
 * 없어 fake를 손으로 만든다).
 */
class RecordingShopChangeHistoryRepository implements ShopChangeHistoryRepository {

    private final List<ShopChangeHistory> saved = new ArrayList<>();

    @Override
    public ShopChangeHistory save(ShopChangeHistory shopChangeHistory) {
        saved.add(shopChangeHistory);
        return shopChangeHistory;
    }

    /** 저장된 이력 전체(저장 순서 그대로). */
    List<ShopChangeHistory> saved() {
        return List.copyOf(saved);
    }

    /** 특정 중분류로 저장된 이력만 골라 본다 — 한 연산이 여러 분류를 남기지 않는지 확인할 때 쓴다. */
    List<ShopChangeHistory> savedOf(ShopChangeType changeType) {
        return saved.stream()
            .filter(history -> history.getChangeType() == changeType)
            .toList();
    }
}
