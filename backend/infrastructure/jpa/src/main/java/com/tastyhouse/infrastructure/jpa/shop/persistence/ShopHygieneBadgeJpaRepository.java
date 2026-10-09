package com.tastyhouse.infrastructure.jpa.shop.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ShopHygieneBadgeJpaRepository extends JpaRepository<ShopHygieneBadgeJpaEntity, Long> {
}
