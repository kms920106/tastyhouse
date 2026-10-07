package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.PopularProductItemResult;

public interface ProductPopularQueryUseCase {

    List<PopularProductItemResult> findPopularProducts(Long shopId);
}
