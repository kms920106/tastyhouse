package com.tastyhouse.infrastructure.persistence.coupon.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface CouponJpaRepository extends JpaRepository<CouponJpaEntity, Long> {
}
