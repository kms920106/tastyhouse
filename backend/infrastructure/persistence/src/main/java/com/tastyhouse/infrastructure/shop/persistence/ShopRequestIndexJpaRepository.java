package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopRequestType;

@Repository
public interface ShopRequestIndexJpaRepository extends JpaRepository<ShopRequestIndexJpaEntity, Long> {

    Optional<ShopRequestIndexJpaEntity> findByRequestTypeAndSourceRequestId(
        ShopRequestType requestType,
        Long sourceRequestId
    );
}
