package com.tastyhouse.core.domain.coupon.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.coupon.domain.model.MemberCoupon;

public interface MemberCouponJpaRepository extends JpaRepository<MemberCoupon, Long> {
}
