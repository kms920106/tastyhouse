package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import com.tastyhouse.core.domain.shop.domain.model.ShopBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopBookmarkJpaRepository extends JpaRepository<ShopBookmark, Long> {
}
