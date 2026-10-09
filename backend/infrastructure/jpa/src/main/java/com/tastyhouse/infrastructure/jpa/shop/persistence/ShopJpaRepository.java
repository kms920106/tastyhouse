package com.tastyhouse.infrastructure.jpa.shop.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ShopJpaRepository extends JpaRepository<ShopJpaEntity, Long> {
}
