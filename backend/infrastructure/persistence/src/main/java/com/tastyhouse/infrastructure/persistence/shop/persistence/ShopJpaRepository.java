package com.tastyhouse.infrastructure.persistence.shop.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ShopJpaRepository extends JpaRepository<ShopJpaEntity, Long> {
}
