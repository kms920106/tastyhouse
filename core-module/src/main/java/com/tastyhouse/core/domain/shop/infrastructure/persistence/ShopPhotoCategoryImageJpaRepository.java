package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.shop.domain.model.ShopPhotoCategoryImage;

@Repository
public interface ShopPhotoCategoryImageJpaRepository extends JpaRepository<ShopPhotoCategoryImage, Long> {

    List<ShopPhotoCategoryImage> findByShopPhotoCategoryId(Long shopPhotoCategoryId);
}
