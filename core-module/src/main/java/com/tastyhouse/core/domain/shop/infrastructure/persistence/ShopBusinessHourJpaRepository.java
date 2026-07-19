package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.shop.domain.model.ShopBusinessHour;

@Repository
public interface ShopBusinessHourJpaRepository extends JpaRepository<ShopBusinessHour, Long> {
}
