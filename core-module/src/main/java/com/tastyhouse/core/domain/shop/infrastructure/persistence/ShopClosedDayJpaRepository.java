package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import com.tastyhouse.core.domain.shop.domain.model.ShopClosedDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopClosedDayJpaRepository extends JpaRepository<ShopClosedDay, Long> {
}
