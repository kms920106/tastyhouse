package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ShopRequestIndexJpaRepository extends JpaRepository<ShopRequestIndexJpaEntity, Long> {

    Optional<ShopRequestIndexJpaEntity> findByRequestTypeAndSourceRequestId(
        String requestType,
        Long sourceRequestId
    );
}
