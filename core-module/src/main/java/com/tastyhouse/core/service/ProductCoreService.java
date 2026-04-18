package com.tastyhouse.core.service;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.entity.product.Product;
import com.tastyhouse.core.entity.product.ProductBbq;
import com.tastyhouse.core.entity.product.ProductCategory;
import com.tastyhouse.core.entity.product.ProductCommonOption;
import com.tastyhouse.core.entity.product.ProductCommonOptionGroup;
import com.tastyhouse.core.entity.product.ProductImage;
import com.tastyhouse.core.entity.product.ProductOption;
import com.tastyhouse.core.entity.product.ProductOptionGroup;
import com.tastyhouse.core.entity.product.dto.TodayDiscountProductDto;
import com.tastyhouse.core.repository.product.ProductBbqJpaRepository;
import com.tastyhouse.core.repository.product.ProductCategoryJpaRepository;
import com.tastyhouse.core.repository.product.ProductCategoryRepository;
import com.tastyhouse.core.repository.product.ProductImageJpaRepository;
import com.tastyhouse.core.repository.product.ProductJpaRepository;
import com.tastyhouse.core.repository.product.ProductOptionGroupJpaRepository;
import com.tastyhouse.core.repository.product.ProductOptionJpaRepository;
import com.tastyhouse.core.repository.product.ProductRepository;
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
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductCategoryJpaRepository productCategoryJpaRepository;
    private final ProductOptionGroupJpaRepository productOptionGroupJpaRepository;
    private final ProductOptionJpaRepository productOptionJpaRepository;
    private final ProductImageJpaRepository productImageJpaRepository;
    private final ProductBbqJpaRepository productBbqJpaRepository;

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

    @Transactional(readOnly = true)
    public List<ProductCategory> findProductCategoriesByNameAndPlaceId(String name, Long placeId) {
        return productCategoryRepository.findByNameAndPlaceId(name, placeId);
    }

    @Transactional
    public ProductCategory saveProductCategory(ProductCategory productCategory) {
        return productCategoryJpaRepository.save(productCategory);
    }

    @Transactional
    public Product saveProduct(Product product) {
        return productJpaRepository.save(product);
    }

    @Transactional
    public ProductImage saveProductImage(ProductImage productImage) {
        return productImageJpaRepository.save(productImage);
    }

    @Transactional
    public ProductBbq saveProductBbq(ProductBbq productBbq) {
        return productBbqJpaRepository.save(productBbq);
    }
}
