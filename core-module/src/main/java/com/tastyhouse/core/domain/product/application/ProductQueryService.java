package com.tastyhouse.core.domain.product.application;

import com.tastyhouse.core.domain.product.application.dto.result.ProductOptionsResult;
import com.tastyhouse.core.domain.product.application.dto.result.SearchProductItemResult;
import com.tastyhouse.core.domain.product.application.dto.result.TodayDiscountProductResult;
import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.domain.product.domain.model.ProductBbq;
import com.tastyhouse.core.domain.product.domain.model.ProductCategory;
import com.tastyhouse.core.domain.product.domain.model.ProductCommonOption;
import com.tastyhouse.core.domain.product.domain.model.ProductCommonOptionGroup;
import com.tastyhouse.core.domain.product.domain.model.ProductImage;
import com.tastyhouse.core.domain.product.domain.model.ProductOption;
import com.tastyhouse.core.domain.product.domain.model.ProductOptionGroup;
import com.tastyhouse.core.domain.product.domain.repository.ProductBbqRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductCategoryRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductCommonOptionGroupRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductCommonOptionRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductImageRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductOptionGroupRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductOptionRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductCommonOptionGroupRepository productCommonOptionGroupRepository;
    private final ProductCommonOptionRepository productCommonOptionRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductBbqRepository productBbqRepository;

    public Optional<Product> findProductById(Long productId) {
        return productRepository.findById(productId);
    }

    public Optional<ProductCategory> findProductCategoryById(Long categoryId) {
        return productCategoryRepository.findById(categoryId);
    }

    public List<Product> findActiveProductsByPlaceId(Long placeId) {
        return productRepository.findActiveByPlaceIdOrderByRepresentativeAndRating(placeId);
    }

    public List<ProductCategory> findProductCategoriesByPlaceId(Long placeId) {
        return productCategoryRepository.findActiveCategoriesByPlaceIdOrderBySort(placeId);
    }

    public List<ProductCategory> findProductCategoriesByNameAndPlaceId(String name, Long placeId) {
        return productCategoryRepository.findCategoriesByNameAndPlaceId(name, placeId);
    }

    public List<ProductOptionGroup> findProductOptionGroupsByProductId(Long productId) {
        return productOptionGroupRepository.findActiveByProductIdOrderBySort(productId);
    }

    public Optional<ProductOptionGroup> findProductOptionGroupById(Long optionGroupId) {
        return productOptionGroupRepository.findById(optionGroupId);
    }

    public Optional<ProductOption> findProductOptionById(Long optionId) {
        return productOptionRepository.findById(optionId);
    }

    public List<ProductOption> findProductOptionsByOptionGroupIds(List<Long> optionGroupIds) {
        return productOptionRepository.findActiveByOptionGroupIdsOrderBySort(optionGroupIds);
    }

    public List<ProductCommonOptionGroup> findProductCommonOptionGroupsByProductId(Long productId) {
        return productCommonOptionGroupRepository.findActiveByProductIdOrderBySort(productId);
    }

    public List<ProductCommonOption> findProductCommonOptionsByOptionGroupIds(List<Long> optionGroupIds) {
        return productCommonOptionRepository.findActiveByOptionGroupIdsOrderBySort(optionGroupIds);
    }

    public List<ProductImage> findActiveImagesByProductId(Long productId) {
        return productImageRepository.findActiveByProductIdOrderBySort(productId);
    }

    public String getFirstImageFilePath(Long productId) {
        return productImageRepository.findActiveByProductIdOrderBySort(productId)
            .stream()
            .findFirst()
            .map(image -> productImageRepository.findFilePathByImageFileId(image.getImageFileId()))
            .orElse(null);
    }

    public List<String> getAllImageFilePaths(Long productId) {
        return productImageRepository.findActiveByProductIdOrderBySort(productId)
            .stream()
            .map(image -> productImageRepository.findFilePathByImageFileId(image.getImageFileId()))
            .toList();
    }

    public Page<TodayDiscountProductResult> findTodayDiscountProducts(int page, int size) {
        return productRepository.findTodayDiscountProducts(PageRequest.of(page, size));
    }

    public Page<SearchProductItemResult> searchByKeyword(String keyword, int page, int size) {
        return productRepository.searchByKeyword(keyword, PageRequest.of(page, size));
    }

    public ProductOptionsResult findProductOptions(Long productId) {
        List<ProductOptionsResult.OptionGroupResult> result = new ArrayList<>();

        List<ProductOptionGroup> optionGroups = productOptionGroupRepository.findActiveByProductIdOrderBySort(productId);
        if (!optionGroups.isEmpty()) {
            List<Long> groupIds = optionGroups.stream().map(ProductOptionGroup::getId).toList();
            List<ProductOption> options = productOptionRepository.findActiveByOptionGroupIdsOrderBySort(groupIds);
            Map<Long, List<ProductOption>> byGroupId = options.stream()
                .collect(Collectors.groupingBy(ProductOption::getOptionGroupId));

            for (ProductOptionGroup group : optionGroups) {
                List<ProductOptionsResult.OptionResult> optionResults = byGroupId
                    .getOrDefault(group.getId(), Collections.emptyList())
                    .stream()
                    .map(o -> new ProductOptionsResult.OptionResult(o.getId(), o.getName(), o.getAdditionalPrice(), o.getIsSoldOut()))
                    .toList();
                result.add(new ProductOptionsResult.OptionGroupResult(
                    group.getId(), group.getName(), group.getDescription(),
                    group.getIsRequired(), group.getIsMultipleSelect(),
                    group.getMinSelect(), group.getMaxSelect(), false, optionResults
                ));
            }
        }

        List<ProductCommonOptionGroup> commonGroups = productCommonOptionGroupRepository.findActiveByProductIdOrderBySort(productId);
        if (!commonGroups.isEmpty()) {
            List<Long> commonGroupIds = commonGroups.stream().map(ProductCommonOptionGroup::getId).toList();
            List<ProductCommonOption> commonOptions = productCommonOptionRepository.findActiveByOptionGroupIdsOrderBySort(commonGroupIds);
            Map<Long, List<ProductCommonOption>> byCommonGroupId = commonOptions.stream()
                .collect(Collectors.groupingBy(ProductCommonOption::getOptionGroupId));

            for (ProductCommonOptionGroup group : commonGroups) {
                List<ProductOptionsResult.OptionResult> optionResults = byCommonGroupId
                    .getOrDefault(group.getId(), Collections.emptyList())
                    .stream()
                    .map(o -> new ProductOptionsResult.OptionResult(o.getId(), o.getName(), o.getAdditionalPrice(), o.getIsSoldOut()))
                    .toList();
                result.add(new ProductOptionsResult.OptionGroupResult(
                    group.getId(), group.getName(), group.getDescription(),
                    group.getIsRequired(), group.getIsMultipleSelect(),
                    group.getMinSelect(), group.getMaxSelect(), true, optionResults
                ));
            }
        }

        return new ProductOptionsResult(result);
    }

    public Optional<ProductBbq> findBbqByProductId(Long productId) {
        return productBbqRepository.findByProductId(productId);
    }

    public Optional<ProductBbq> findFirstBbqWithOptionsSyncPending() {
        return productBbqRepository.findFirstWithOptionsSyncPending();
    }
}
