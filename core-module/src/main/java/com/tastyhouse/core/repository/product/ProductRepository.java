package com.tastyhouse.core.repository.product;

import com.tastyhouse.core.entity.product.Product;
import com.tastyhouse.core.entity.product.ProductBbq;
import com.tastyhouse.core.entity.product.ProductCategory;
import com.tastyhouse.core.entity.product.ProductCommonOption;
import com.tastyhouse.core.entity.product.ProductCommonOptionGroup;
import com.tastyhouse.core.entity.product.ProductImage;
import com.tastyhouse.core.entity.product.ProductOption;
import com.tastyhouse.core.entity.product.ProductOptionGroup;
import com.tastyhouse.core.entity.product.dto.ProductSimpleDto;
import com.tastyhouse.core.entity.product.dto.SearchProductItemDto;
import com.tastyhouse.core.entity.product.dto.TodayDiscountProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Page<TodayDiscountProductDto> findTodayDiscountProducts(Pageable pageable);

    Page<SearchProductItemDto> searchByKeyword(String keyword, Pageable pageable);

    List<ProductSimpleDto> findProductsByPlaceId(Long placeId);

    List<Product> findByPlaceIdOrderByRepresentativeAndRating(Long placeId);

    List<Product> findActiveByPlaceIdOrderByRepresentativeAndRating(Long placeId);

    List<Product> findByPlaceId(Long placeId);

    List<ProductCategory> findActiveCategoriesByPlaceIdOrderBySort(Long placeId);

    List<ProductCategory> findCategoriesByNameAndPlaceId(String name, Long placeId);

    List<ProductImage> findActiveImagesByProductIdOrderBySort(Long productId);

    List<ProductImage> findImagesByProductIdOrderBySort(Long productId);

    List<ProductOptionGroup> findActiveOptionGroupsByProductIdOrderBySort(Long productId);

    boolean existsOptionGroupByProductId(Long productId);

    List<ProductOption> findActiveOptionsByOptionGroupIdOrderBySort(Long optionGroupId);

    List<ProductOption> findActiveOptionsByOptionGroupIdsOrderBySort(List<Long> optionGroupIds);

    List<ProductCommonOptionGroup> findActiveCommonOptionGroupsByProductIdOrderBySort(Long productId);

    List<ProductCommonOption> findActiveCommonOptionsByOptionGroupIdOrderBySort(Long optionGroupId);

    List<ProductCommonOption> findActiveCommonOptionsByOptionGroupIdsOrderBySort(List<Long> optionGroupIds);

    Optional<ProductBbq> findBbqByProductId(Long productId);

    boolean existsBbqByProductId(Long productId);

    Optional<ProductBbq> findFirstBbqWithOptionsSyncPending();

    String findFilePathByImageFileId(Long imageFileId);
}
