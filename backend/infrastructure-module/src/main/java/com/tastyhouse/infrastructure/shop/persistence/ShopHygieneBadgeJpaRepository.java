package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.vo.ShopId;

@Repository
public interface ShopHygieneBadgeJpaRepository extends JpaRepository<ShopHygieneBadgeJpaEntity, Long> {

    List<ShopHygieneBadgeJpaEntity> findByShopId(ShopId shopId);
}
