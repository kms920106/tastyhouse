package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRequestIndexJpaRepository extends JpaRepository<ShopRequestIndexJpaEntity, Long> {
    Optional<ShopRequestIndexJpaEntity> findByRequestTypeAndSourceRequestId(
        String requestType,
        Long sourceRequestId
    );
}
