package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.domain.shared.model.ApprovalStatus;

public interface ProductRepresentativeRequestJpaRepository
    extends JpaRepository<ProductRepresentativeRequestJpaEntity, Long> {

    List<ProductRepresentativeRequestJpaEntity> findAllByProductId(Long productId);

    boolean existsByProductIdAndStatus(Long productId, ApprovalStatus status);

    long countByShopIdAndStatus(Long shopId, ApprovalStatus status);
}
