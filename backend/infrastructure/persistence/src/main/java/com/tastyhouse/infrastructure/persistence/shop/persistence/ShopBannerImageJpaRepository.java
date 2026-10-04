package com.tastyhouse.infrastructure.persistence.shop.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopBannerImageJpaRepository extends JpaRepository<ShopBannerImageJpaEntity, Long> {
}
