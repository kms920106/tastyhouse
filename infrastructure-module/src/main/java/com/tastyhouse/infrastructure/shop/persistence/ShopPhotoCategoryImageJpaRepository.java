package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopPhotoCategoryImageJpaRepository extends JpaRepository<ShopPhotoCategoryImageJpaEntity, Long> {

    List<ShopPhotoCategoryImageJpaEntity> findByShopPhotoCategoryId(Long shopPhotoCategoryId);
}
