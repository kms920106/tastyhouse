package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;

public interface StorePriceVerificationJpaRepository
    extends JpaRepository<StorePriceVerificationJpaEntity, Long> {

    Optional<StorePriceVerificationJpaEntity> findFirstByShopIdOrderByIdDesc(Long shopId);

    boolean existsByShopIdAndStatusIn(Long shopId, List<StorePriceVerificationStatus> statuses);
}
