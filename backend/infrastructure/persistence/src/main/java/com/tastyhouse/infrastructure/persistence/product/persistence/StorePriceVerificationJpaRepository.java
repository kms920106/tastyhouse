package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface StorePriceVerificationJpaRepository
    extends JpaRepository<StorePriceVerificationJpaEntity, Long> {

    Optional<StorePriceVerificationJpaEntity> findFirstByShopIdOrderByIdDesc(Long shopId);

    boolean existsByShopIdAndStatusIn(Long shopId, List<String> statuses);
}
