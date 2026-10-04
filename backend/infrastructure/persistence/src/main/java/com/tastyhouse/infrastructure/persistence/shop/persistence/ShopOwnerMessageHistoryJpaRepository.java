package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ShopOwnerMessageHistoryJpaRepository extends JpaRepository<ShopOwnerMessageHistoryJpaEntity, Long> {

    Optional<ShopOwnerMessageHistoryJpaEntity> findFirstByShopIdOrderByIdDesc(Long shopId);
}
