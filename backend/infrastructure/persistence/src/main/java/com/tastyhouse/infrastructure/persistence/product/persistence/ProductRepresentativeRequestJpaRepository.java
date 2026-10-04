package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepresentativeRequestJpaRepository
    extends JpaRepository<ProductRepresentativeRequestJpaEntity, Long> {

    List<ProductRepresentativeRequestJpaEntity> findAllByProductId(Long productId);

    boolean existsByProductIdAndStatus(Long productId, String status);

    long countByShopIdAndStatus(Long shopId, String status);
}
