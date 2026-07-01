package com.tastyhouse.core.domain.product.domain.repository;

import com.tastyhouse.core.domain.product.application.dto.result.SearchProductItemResult;
import com.tastyhouse.core.domain.product.application.dto.result.TodayDiscountProductResult;
import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    PageResult<TodayDiscountProductResult> findTodayDiscountProducts(PageQuery pageQuery);

    PageResult<SearchProductItemResult> searchByKeyword(String keyword, PageQuery pageQuery);

    List<Product> findActiveByShopIdOrderByRepresentativeAndRating(Long shopId);

    Optional<Product> findById(Long id);

    List<Product> findAllByIds(List<Long> ids);

    Product save(Product product);
}
