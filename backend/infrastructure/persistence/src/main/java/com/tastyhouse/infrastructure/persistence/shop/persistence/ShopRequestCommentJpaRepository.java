package com.tastyhouse.infrastructure.persistence.shop.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ShopRequestCommentJpaRepository extends JpaRepository<ShopRequestCommentJpaEntity, Long> {
}
