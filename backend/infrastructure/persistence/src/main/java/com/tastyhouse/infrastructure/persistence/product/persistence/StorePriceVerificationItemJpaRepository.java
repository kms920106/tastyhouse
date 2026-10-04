package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface StorePriceVerificationItemJpaRepository
    extends JpaRepository<StorePriceVerificationItemJpaEntity, Long> {

    List<StorePriceVerificationItemJpaEntity> findAllByVerificationIdOrderByIdAsc(Long verificationId);
}
