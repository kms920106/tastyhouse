package com.tastyhouse.infrastructure.persistence.coupon.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface MemberCouponJpaRepository extends JpaRepository<MemberCouponJpaEntity, Long> {
}
