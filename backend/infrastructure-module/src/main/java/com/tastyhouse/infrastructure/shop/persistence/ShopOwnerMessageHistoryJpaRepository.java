package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopOwnerMessageHistoryJpaRepository extends JpaRepository<ShopOwnerMessageHistoryJpaEntity, Long> {

    /**
     * 가게의 가장 최근 사장님 한마디. append-only 이력이라 최신 행이 곧 현재 노출 문구다.
     */
    Optional<ShopOwnerMessageHistoryJpaEntity> findFirstByShopIdOrderByIdDesc(Long shopId);
}
