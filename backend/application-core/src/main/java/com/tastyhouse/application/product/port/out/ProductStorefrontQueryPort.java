package com.tastyhouse.application.product.port.out;

import java.util.List;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ProductStorefrontQueryPort {

    PageResult<TodayDiscountProductResult> findTodayDiscountProducts(ProductExposureWindow window, PageQuery pageQuery);

    PageResult<SearchProductItemResult> searchByKeyword(String keyword, ProductExposureWindow window, PageQuery pageQuery);

    List<ShopProductItemResult> findShopProducts(Long shopId, ProductExposureWindow window);

    List<PopularProductItemResult> findPopularProducts(Long shopId, String soldOrderStatus, ProductExposureWindow window);
}
