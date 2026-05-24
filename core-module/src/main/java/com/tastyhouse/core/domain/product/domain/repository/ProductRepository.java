package com.tastyhouse.core.domain.product.domain.repository;

import com.tastyhouse.core.domain.product.application.dto.result.ProductSimpleResult;
import com.tastyhouse.core.domain.product.application.dto.result.SearchProductItemResult;
import com.tastyhouse.core.domain.product.application.dto.result.TodayDiscountProductResult;
import com.tastyhouse.core.domain.product.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Page<TodayDiscountProductResult> findTodayDiscountProducts(Pageable pageable);

    Page<SearchProductItemResult> searchByKeyword(String keyword, Pageable pageable);

    List<ProductSimpleResult> findProductsByPlaceId(Long placeId);

    List<Product> findByPlaceIdOrderByRepresentativeAndRating(Long placeId);

    List<Product> findActiveByPlaceIdOrderByRepresentativeAndRating(Long placeId);

    List<Product> findByPlaceId(Long placeId);

    Optional<Product> findById(Long id);

    boolean existsById(Long id);

    Product save(Product product);
}
