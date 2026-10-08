package com.tastyhouse.infrastructure.persistence.shop.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ShopOriginInfoJpaRepository extends JpaRepository<ShopOriginInfoJpaEntity, Long> {
}
