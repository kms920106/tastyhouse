package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.domain.shared.model.ApprovalStatus;

public interface ProductImageChangeRequestJpaRepository
    extends JpaRepository<ProductImageChangeRequestJpaEntity, Long> {

    List<ProductImageChangeRequestJpaEntity> findAllByProductId(Long productId);

    boolean existsByProductIdAndStatus(Long productId, ApprovalStatus status);
}
