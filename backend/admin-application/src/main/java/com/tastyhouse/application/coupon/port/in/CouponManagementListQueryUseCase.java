package com.tastyhouse.application.coupon.port.in;

import com.tastyhouse.application.coupon.port.out.CouponListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface CouponManagementListQueryUseCase {

    PageResult<CouponListItemResult> getCoupons(
        String name,
        String discountType,
        Boolean visible,
        int page,
        int size
    );
}
