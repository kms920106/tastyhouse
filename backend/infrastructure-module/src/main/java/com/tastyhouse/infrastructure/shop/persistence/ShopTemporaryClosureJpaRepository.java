package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.vo.ShopId;

@Repository
public interface ShopTemporaryClosureJpaRepository extends JpaRepository<ShopTemporaryClosureJpaEntity, Long> {

    List<ShopTemporaryClosureJpaEntity> findByShopId(ShopId shopId);
}
