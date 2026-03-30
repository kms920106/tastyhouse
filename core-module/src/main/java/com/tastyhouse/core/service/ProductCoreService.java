package com.tastyhouse.core.service;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.entity.product.*;
import com.tastyhouse.core.entity.product.dto.TodayDiscountProductDto;
import com.tastyhouse.core.repository.product.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCoreService {

    private final ProductRepository productRepository;
    private final ProductJpaRepository productJpaRepository;
    private final ProductCategoryJpaRepository productCategoryJpaRepository;
    private final ProductOptionGroupJpaRepository productOptionGroupJpaRepository;
    private final ProductOptionJpaRepository productOptionJpaRepository;

    @Transactional(readOnly = true)
    public PageResult<TodayDiscountProductDto> findTodayDiscountProducts(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<TodayDiscountProductDto> productPage = productRepository.findTodayDiscountProducts(pageRequest);
        return PageResult.from(productPage);
    }

    @Transactional(readOnly = true)
    public List<Product> findProductsByPlaceId(Long placeId) {
        return productRepository.findActiveByPlaceIdOrderByRepresentativeAndRating(placeId);
    }

    @Transactional(readOnly = true)
    public List<Product> findActiveProductsByPlaceId(Long placeId) {
        return productRepository.findActiveByPlaceIdOrderBySort(placeId);
    }

    @Transactional(readOnly = true)
    public Optional<Product> findProductById(Long productId) {
        return productJpaRepository.findById(productId);
    }

    @Transactional(readOnly = true)
    public Optional<ProductCategory> findProductCategoryById(Long categoryId) {
        return productCategoryJpaRepository.findById(categoryId);
    }

    @Transactional(readOnly = true)
    public List<ProductCategory> findProductCategoriesByPlaceId(Long placeId) {
        return productRepository.findActiveCategoriesByPlaceIdOrderBySort(placeId);
    }

    @Transactional(readOnly = true)
    public List<ProductOptionGroup> findProductOptionGroupsByProductId(Long productId) {
        return productRepository.findActiveOptionGroupsByProductIdOrderBySort(productId);
    }

    @Transactional(readOnly = true)
    public Optional<ProductOptionGroup> findProductOptionGroupById(Long optionGroupId) {
        return productOptionGroupJpaRepository.findById(optionGroupId);
    }

    @Transactional(readOnly = true)
    public Optional<ProductOption> findProductOptionById(Long optionId) {
        return productOptionJpaRepository.findById(optionId);
    }

    @Transactional(readOnly = true)
    public List<ProductOption> findProductOptionsByOptionGroupIds(List<Long> optionGroupIds) {
        return productRepository.findActiveOptionsByOptionGroupIdsOrderBySort(optionGroupIds);
    }

    @Transactional(readOnly = true)
    public List<ProductCommonOptionGroup> findProductCommonOptionGroupsByProductId(Long productId) {
        return productRepository.findActiveCommonOptionGroupsByProductIdOrderBySort(productId);
    }

    @Transactional(readOnly = true)
    public List<ProductCommonOption> findProductCommonOptionsByOptionGroupIds(List<Long> optionGroupIds) {
        return productRepository.findActiveCommonOptionsByOptionGroupIdsOrderBySort(optionGroupIds);
    }

    @Transactional(readOnly = true)
    public String getFirstImageUrl(Long productId) {
        return productRepository.findActiveImagesByProductIdOrderBySort(productId)
            .stream()
            .findFirst()
            .map(ProductImage::getImageUrl)
            .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<String> getAllImageUrls(Long productId) {
        return productRepository.findActiveImagesByProductIdOrderBySort(productId)
            .stream()
            .map(ProductImage::getImageUrl)
            .toList();
    }
}
