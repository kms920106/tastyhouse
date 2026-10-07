package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.TodayDiscountProductResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ProductTodayDiscountQueryUseCase {

    PageResult<TodayDiscountProductResult> searchTodayDiscountProducts(int page, int size);
}
