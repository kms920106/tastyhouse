package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopHygieneBadgeJpaRepository extends JpaRepository<ShopHygieneBadgeJpaEntity, Long> {

    List<ShopHygieneBadgeJpaEntity> findByShopId(Long shopId);
}
