package com.tastyhouse.infrastructure.jpa.coupon.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface MemberCouponJpaRepository extends JpaRepository<MemberCouponJpaEntity, Long> {
}
