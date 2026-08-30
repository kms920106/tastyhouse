package com.tastyhouse.infrastructure.shop.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopAmenityCategoryJpaRepository extends JpaRepository<ShopAmenityCategoryJpaEntity, Long> {
}
