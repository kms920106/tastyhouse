package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import com.tastyhouse.core.domain.shop.domain.model.ShopOrderMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopOrderMethodJpaRepository extends JpaRepository<ShopOrderMethod, Long> {
}
