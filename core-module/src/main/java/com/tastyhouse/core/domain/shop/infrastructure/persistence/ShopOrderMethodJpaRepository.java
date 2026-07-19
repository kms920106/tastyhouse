package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.core.domain.shop.domain.model.ShopOrderMethod;

@Repository
public interface ShopOrderMethodJpaRepository extends JpaRepository<ShopOrderMethod, Long> {

    void deleteByShopIdAndOrderMethod(Long shopId, OrderMethod orderMethod);
}
