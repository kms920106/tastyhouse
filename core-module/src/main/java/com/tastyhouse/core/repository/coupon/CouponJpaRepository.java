package com.tastyhouse.core.repository.coupon;

import com.tastyhouse.core.entity.coupon.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {
}
