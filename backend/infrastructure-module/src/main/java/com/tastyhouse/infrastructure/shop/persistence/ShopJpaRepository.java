package com.tastyhouse.infrastructure.shop.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopJpaRepository extends JpaRepository<ShopJpaEntity, Long> {
}
