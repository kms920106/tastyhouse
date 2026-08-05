package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.vo.ShopId;

@Repository
public interface ShopContentBoardJpaRepository extends JpaRepository<ShopContentBoardJpaEntity, Long> {

    List<ShopContentBoardJpaEntity> findByShopId(ShopId shopId);

    long countByShopId(ShopId shopId);
}
