package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductShopLinkJpaRepository extends JpaRepository<ProductShopLinkJpaEntity, Long> {

    Optional<ProductShopLinkJpaEntity> findByProductIdAndShopId(Long productId, Long shopId);

    List<ProductShopLinkJpaEntity> findAllByProductId(Long productId);

    List<ProductShopLinkJpaEntity> findAllByShopIdOrderBySortAsc(Long shopId);

    boolean existsByProductIdAndShopId(Long productId, Long shopId);

    long countByProductId(Long productId);
}
