package com.tastyhouse.webapi.product;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.domain.review.application.ReviewQueryService;
import com.tastyhouse.core.domain.review.application.dto.result.LatestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.ProductReviewStatisticsResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewsByRatingResult;
import com.tastyhouse.core.entity.product.Product;
import com.tastyhouse.core.entity.product.ProductCommonOption;
import com.tastyhouse.core.entity.product.ProductCommonOptionGroup;
import com.tastyhouse.core.entity.product.ProductOption;
import com.tastyhouse.core.entity.product.ProductOptionGroup;
import com.tastyhouse.core.entity.product.dto.TodayDiscountProductDto;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.service.ProductCoreService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.product.response.ProductDetailResponse;
import com.tastyhouse.webapi.product.response.ProductImagesResponse;
import com.tastyhouse.webapi.product.response.ProductOptionGroupsResponse;
import com.tastyhouse.webapi.product.response.ProductReviewCountResponse;
import com.tastyhouse.webapi.product.response.ProductReviewListItemResponse;
import com.tastyhouse.webapi.product.response.ProductReviewStatisticsResponse;
import com.tastyhouse.webapi.product.response.ProductReviewsByRatingResponse;
import com.tastyhouse.webapi.product.response.ProductReviewsByRatingWithPagination;
import com.tastyhouse.webapi.product.response.TodayDiscountProductListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductCoreService productCoreService;
    private final ReviewQueryService reviewQueryService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public PageResult<TodayDiscountProductListItemResponse> searchTodayDiscountProducts(int page, int size) {
        return PageResult.from(productCoreService.findTodayDiscountProducts(page, size))
            .map(this::convertToTodayDiscountProductListItemResponse);
    }

    private TodayDiscountProductListItemResponse convertToTodayDiscountProductListItemResponse(TodayDiscountProductDto dto) {
        return TodayDiscountProductListItemResponse.from(
            dto.id(),
            dto.placeName(),
            dto.name(),
            fileService.getUrlByPath(dto.imageUrl()),
            dto.originalPrice(),
            dto.discountPrice(),
            dto.discountRate()
        );
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse findProductById(Long productId) {
        Product product = productCoreService.findProductById(productId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductDetailResponse.from(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getOriginalPrice(),
            product.getDiscountPrice(),
            product.getDiscountRate(),
            product.getIsSoldOut()
        );
    }

    @Transactional(readOnly = true)
    public ProductReviewCountResponse findProductReviewCount(Long productId) {
        productCoreService.findProductById(productId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        ProductReviewStatisticsResult statistics = reviewQueryService.findProductReviewStatistics(productId);
        Long total = statistics.totalReviewCount();
        return ProductReviewCountResponse.from(total != null ? total.intValue() : 0);
    }

    @Transactional(readOnly = true)
    public ProductOptionGroupsResponse findProductOptions(Long productId) {
        productCoreService.findProductById(productId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductOptionGroupsResponse.from(buildOptionGroups(productId));
    }

    private List<ProductOptionGroupsResponse.OptionGroupResponse> buildOptionGroups(Long productId) {
        List<ProductOptionGroupsResponse.OptionGroupResponse> result = new ArrayList<>();

        List<ProductOptionGroup> productOptionGroups = productCoreService.findProductOptionGroupsByProductId(productId);
        if (!productOptionGroups.isEmpty()) {
            List<Long> optionGroupIds = productOptionGroups.stream()
                .map(ProductOptionGroup::getId)
                .toList();
            List<ProductOption> productOptions = productCoreService.findProductOptionsByOptionGroupIds(optionGroupIds);
            Map<Long, List<ProductOption>> optionsByGroupId = productOptions.stream()
                .collect(Collectors.groupingBy(ProductOption::getOptionGroupId));

            for (ProductOptionGroup group : productOptionGroups) {
                List<ProductOptionGroupsResponse.OptionResponse> options = optionsByGroupId
                    .getOrDefault(group.getId(), Collections.emptyList())
                    .stream()
                    .map(o -> toOptionResponse(o.getId(), o.getName(), o.getAdditionalPrice(), o.getIsSoldOut()))
                    .toList();

                result.add(ProductOptionGroupsResponse.OptionGroupResponse.from(
                    group.getId(),
                    group.getName(),
                    group.getDescription(),
                    group.getIsRequired(),
                    group.getIsMultipleSelect(),
                    group.getMinSelect(),
                    group.getMaxSelect(),
                    false,
                    options
                ));
            }
        }

        List<ProductCommonOptionGroup> productCommonOptionGroups = productCoreService.findProductCommonOptionGroupsByProductId(productId);
        if (!productCommonOptionGroups.isEmpty()) {
            List<Long> commonOptionGroupIds = productCommonOptionGroups.stream()
                .map(ProductCommonOptionGroup::getId)
                .toList();
            List<ProductCommonOption> commonOptions = productCoreService.findProductCommonOptionsByOptionGroupIds(commonOptionGroupIds);
            Map<Long, List<ProductCommonOption>> commonOptionsByGroupId = commonOptions.stream()
                .collect(Collectors.groupingBy(ProductCommonOption::getOptionGroupId));

            for (ProductCommonOptionGroup group : productCommonOptionGroups) {
                List<ProductOptionGroupsResponse.OptionResponse> options = commonOptionsByGroupId
                    .getOrDefault(group.getId(), Collections.emptyList())
                    .stream()
                    .map(o -> toOptionResponse(o.getId(), o.getName(), o.getAdditionalPrice(), o.getIsSoldOut()))
                    .toList();

                result.add(ProductOptionGroupsResponse.OptionGroupResponse.from(
                    group.getId(),
                    group.getName(),
                    group.getDescription(),
                    group.getIsRequired(),
                    group.getIsMultipleSelect(),
                    group.getMinSelect(),
                    group.getMaxSelect(),
                    true,
                    options
                ));
            }
        }

        return result;
    }

    private ProductOptionGroupsResponse.OptionResponse toOptionResponse(
        Long id, String name, Integer additionalPrice, Boolean isSoldOut
    ) {
        return ProductOptionGroupsResponse.OptionResponse.from(id, name, additionalPrice, isSoldOut);
    }

    @Transactional(readOnly = true)
    public ProductImagesResponse findProductImages(Long productId) {
        productCoreService.findProductById(productId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductImagesResponse.from(getAllImageUrls(productId));
    }

    private List<String> getAllImageUrls(Long productId) {
        return productCoreService.getAllImageFilePaths(productId).stream()
            .map(fileService::getUrlByPath)
            .toList();
    }

    @Transactional(readOnly = true)
    public ProductReviewsByRatingWithPagination getProductReviewsByRatingWithPagination(Long productId, int page, int size) {
        ReviewsByRatingResult result = reviewQueryService.findProductReviewsByRating(productId, page, size);

        Map<Integer, List<ProductReviewListItemResponse>> reviewsByRating = result.getReviewsByRating().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(this::convertToProductReviewListItemResponse)
                                .toList()
                ));

        List<ProductReviewListItemResponse> allReviews = result.getAllReviews().stream()
                .map(this::convertToProductReviewListItemResponse)
                .toList();

        ProductReviewsByRatingResponse response = ProductReviewsByRatingResponse.from(
            reviewsByRating,
            allReviews,
            result.getTotalReviewCount()
        );

        return new ProductReviewsByRatingWithPagination(response, result.getTotalElements());
    }

    private ProductReviewListItemResponse convertToProductReviewListItemResponse(LatestReviewListItemResult dto) {
        return ProductReviewListItemResponse.from(
            dto.id(),
            dto.imageUrls().stream().map(fileService::getUrlByPath).toList(),
            dto.totalRating(),
            dto.content(),
            dto.memberNickname(),
            fileService.getUrlByPath(dto.memberProfileImageUrl()),
            dto.createdAt(),
            dto.productId(),
            dto.productName()
        );
    }

    @Transactional(readOnly = true)
    public ProductReviewStatisticsResponse getProductReviewStatistics(Long productId) {
        ProductReviewStatisticsResult statistics = reviewQueryService.findProductReviewStatistics(productId);

        Product product = productCoreService.findProductById(productId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        return ProductReviewStatisticsResponse.from(
            product.getRating(),
            statistics.totalReviewCount(),
            statistics.averageTasteRating(),
            statistics.averageAmountRating(),
            statistics.averagePriceRating()
        );
    }
}
