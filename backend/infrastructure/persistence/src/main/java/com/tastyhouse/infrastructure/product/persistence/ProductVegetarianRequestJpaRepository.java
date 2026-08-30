package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.domain.shared.model.ApprovalStatus;

public interface ProductVegetarianRequestJpaRepository
    extends JpaRepository<ProductVegetarianRequestJpaEntity, Long> {

    List<ProductVegetarianRequestJpaEntity> findAllByProductId(Long productId);

    boolean existsByProductIdAndStatus(Long productId, ApprovalStatus status);
}
