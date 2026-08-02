package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopPhoneNumberJpaRepository extends JpaRepository<ShopPhoneNumberJpaEntity, Long> {

    List<ShopPhoneNumberJpaEntity> findByShopId(Long shopId);
}
