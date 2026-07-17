package com.tastyhouse.core.domain.coupon.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.coupon.domain.model.Coupon;
import com.tastyhouse.core.domain.coupon.domain.vo.CouponId;
import com.tastyhouse.core.domain.coupon.application.dto.CouponSearchCondition;
import com.tastyhouse.core.domain.coupon.application.dto.result.CouponListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface CouponRepository {

    Optional<Coupon> findById(CouponId id);

    PageResult<CouponListItemResult> findAllCoupons(CouponSearchCondition condition, PageQuery pageQuery);

    Coupon save(Coupon coupon);
}
