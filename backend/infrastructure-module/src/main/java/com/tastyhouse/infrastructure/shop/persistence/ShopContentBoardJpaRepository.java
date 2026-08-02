package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopContentBoardJpaRepository extends JpaRepository<ShopContentBoardJpaEntity, Long> {

    List<ShopContentBoardJpaEntity> findByShopId(Long shopId);

    long countByShopId(Long shopId);
}
