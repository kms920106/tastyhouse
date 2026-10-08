package com.tastyhouse.infrastructure.persistence.shop.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ShopNoticeJpaRepository extends JpaRepository<ShopNoticeJpaEntity, Long> {
}
