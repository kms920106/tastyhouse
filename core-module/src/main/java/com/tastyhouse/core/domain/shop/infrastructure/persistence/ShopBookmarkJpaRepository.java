package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.shop.domain.model.ShopBookmark;

public interface ShopBookmarkJpaRepository extends JpaRepository<ShopBookmark, Long> {
}
