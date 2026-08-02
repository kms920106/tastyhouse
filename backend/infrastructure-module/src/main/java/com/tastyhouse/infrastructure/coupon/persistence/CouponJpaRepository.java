package com.tastyhouse.infrastructure.coupon.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponJpaRepository extends JpaRepository<CouponJpaEntity, Long> {
}
