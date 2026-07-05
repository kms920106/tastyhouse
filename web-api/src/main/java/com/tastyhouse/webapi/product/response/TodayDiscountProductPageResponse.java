package com.tastyhouse.webapi.product.response;

import java.util.List;

import com.tastyhouse.core.shared.page.PageResult;

public record TodayDiscountProductPageResponse(
    List<TodayDiscountProductListItemResponse> content,
    int page,
    int size,
    long totalElements
) {

    public static TodayDiscountProductPageResponse from(PageResult<TodayDiscountProductListItemResponse> pageResult) {
        return new TodayDiscountProductPageResponse(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements());
    }
}
