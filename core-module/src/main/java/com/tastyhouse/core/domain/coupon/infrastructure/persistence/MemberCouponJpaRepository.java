package com.tastyhouse.core.domain.coupon.infrastructure.persistence;

import com.tastyhouse.core.domain.coupon.domain.model.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCouponJpaRepository extends JpaRepository<MemberCoupon, Long> {
}
