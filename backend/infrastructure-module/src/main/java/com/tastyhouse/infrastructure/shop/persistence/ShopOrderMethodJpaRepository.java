package com.tastyhouse.infrastructure.shop.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.OrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;

@Repository
public interface ShopOrderMethodJpaRepository extends JpaRepository<ShopOrderMethodJpaEntity, Long> {

    void deleteByShopIdAndOrderMethod(ShopId shopId, OrderMethod orderMethod);
}
