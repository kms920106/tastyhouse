package com.tastyhouse.core.domain.coupon.infrastructure.persistence;

import com.tastyhouse.core.domain.coupon.domain.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {
}
