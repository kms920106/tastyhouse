package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ShopHygieneBadgeJpaRepository extends JpaRepository<ShopHygieneBadgeJpaEntity, Long> {

    List<ShopHygieneBadgeJpaEntity> findByShopId(Long shopId);
}
