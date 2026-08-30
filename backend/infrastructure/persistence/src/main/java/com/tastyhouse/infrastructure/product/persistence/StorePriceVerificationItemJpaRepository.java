package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StorePriceVerificationItemJpaRepository
    extends JpaRepository<StorePriceVerificationItemJpaEntity, Long> {

    List<StorePriceVerificationItemJpaEntity> findAllByVerificationIdOrderByIdAsc(Long verificationId);
}
