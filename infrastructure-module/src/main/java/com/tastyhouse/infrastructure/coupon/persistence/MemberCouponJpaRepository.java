package com.tastyhouse.infrastructure.coupon.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCouponJpaRepository extends JpaRepository<MemberCouponJpaEntity, Long> {
}
