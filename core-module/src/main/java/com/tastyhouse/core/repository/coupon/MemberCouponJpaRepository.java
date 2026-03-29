package com.tastyhouse.core.repository.coupon;

import com.tastyhouse.core.entity.coupon.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCouponJpaRepository extends JpaRepository<MemberCoupon, Long> {
}
