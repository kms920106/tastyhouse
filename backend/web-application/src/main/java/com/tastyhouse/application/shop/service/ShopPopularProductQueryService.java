package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductPopularQueryUseCase;
import com.tastyhouse.application.product.port.out.PopularProductItemResult;
import com.tastyhouse.application.shop.port.in.ShopPopularProductQueryUseCase;

@Service
@Transactional(readOnly = true)
class ShopPopularProductQueryService implements ShopPopularProductQueryUseCase {

    private final ProductPopularQueryUseCase productPopularQueryUseCase;

    public ShopPopularProductQueryService(ProductPopularQueryUseCase productPopularQueryUseCase) {
        this.productPopularQueryUseCase = productPopularQueryUseCase;
    }

    @Override
    public List<PopularProductItemResult> getPopularProducts(Long shopId) {
        return productPopularQueryUseCase.findPopularProducts(shopId);
    }
}
