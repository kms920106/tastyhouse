package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.shop.domain.model.ShopBannerImage;

@Repository
public interface ShopBannerImageJpaRepository extends JpaRepository<ShopBannerImage, Long> {
}
