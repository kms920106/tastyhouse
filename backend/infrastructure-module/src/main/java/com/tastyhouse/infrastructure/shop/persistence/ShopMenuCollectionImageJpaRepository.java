package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopMenuCollectionImageJpaRepository
    extends JpaRepository<ShopMenuCollectionImageJpaEntity, Long> {

    /** 상태 무관 전량 — 개수 제한·최소 1개 유지 같은 집합 불변식이 대기·반려 건까지 포함해 판정된다. */
    List<ShopMenuCollectionImageJpaEntity> findAllByShopIdOrderBySortAsc(Long shopId);
}
