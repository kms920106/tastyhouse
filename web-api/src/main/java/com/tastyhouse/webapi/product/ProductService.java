package com.tastyhouse.webapi.product;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.common.ReviewsByRatingResult;
import com.tastyhouse.core.entity.product.*;
import com.tastyhouse.core.entity.product.dto.TodayDiscountProductDto;
import com.tastyhouse.core.entity.review.dto.LatestReviewListItemDto;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.service.PlaceCoreService;
import com.tastyhouse.core.service.ProductCoreService;
import com.tastyhouse.core.service.ReviewCoreService;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.product.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductCoreService productCoreService;
    private final PlaceCoreService placeCoreService;
    private final ReviewCoreService reviewCoreService;

    @Transactional(readOnly = true)
    public PageResult<TodayDiscountProductItem> searchTodayDiscountProducts(PageRequest pageRequest) {
        return productCoreService.findTodayDiscountProducts(
            pageRequest.page(), pageRequest.size()
        ).map(this::convertToTodayDiscountProductItem);
    }

    private TodayDiscountProductItem convertToTodayDiscountProductItem(TodayDiscountProductDto dto) {
        return new TodayDiscountProductItem(
            dto.id(), dto.placeName(), dto.name(), dto.imageUrl(),
            dto.originalPrice(), dto.discountPrice(), dto.discountRate()
        );
    }

    @Transactional(readOnly = true)
    public List<ProductListItem> searchProductsByPlaceId(Long placeId) {
        List<Product> products = productCoreService.findActiveProductsByPlaceId(placeId);
        Map<Long, String> categoryNameMap = buildCategoryNameMap(placeId);

        return products.stream()
            .map(product -> convertToProductListItem(product, categoryNameMap))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductCategoryListItem> searchProductCategoriesByPlaceId(Long placeId) {
        List<ProductCategory> categories = productCoreService.findProductCategoriesByPlaceId(placeId);
        return categories.stream()
            .map(this::convertToProductCategoryListItem)
            .toList();
    }

    private ProductCategoryListItem convertToProductCategoryListItem(ProductCategory category) {
        return new ProductCategoryListItem(category.getId(), category.getName(), category.getSort());
    }

    @Transactional(readOnly = true)
    public Optional<ProductDetailResponse> findProductById(Long productId) {
        return productCoreService.findProductById(productId)
            .map(this::buildProductDetailResponse);
    }

    private Map<Long, String> buildCategoryNameMap(Long placeId) {
        List<ProductCategory> categories = productCoreService.findProductCategoriesByPlaceId(placeId);
        return categories.stream()
            .collect(Collectors.toMap(ProductCategory::getId, ProductCategory::getName));
    }

    private ProductListItem convertToProductListItem(Product product, Map<Long, String> categoryNameMap) {
        String categoryName = product.getProductCategoryId() != null
            ? categoryNameMap.get(product.getProductCategoryId())
            : null;

        String imageUrl = getFirstImageUrl(product.getId());

        return new ProductListItem(
            product.getId(), product.getName(), product.getDescription(), imageUrl,
            product.getOriginalPrice(), product.getDiscountPrice(), product.getDiscountRate(),
            product.getRating(), product.getReviewCount(), product.getIsRepresentative(),
            product.getIsSoldOut(), categoryName
        );
    }

    private ProductDetailResponse buildProductDetailResponse(Product product) {
        String placeName = placeCoreService.findPlaceById(product.getPlaceId()).getName();

        String categoryName = null;
        if (product.getProductCategoryId() != null) {
            categoryName = productCoreService.findProductCategoryById(product.getProductCategoryId())
                .map(ProductCategory::getName)
                .orElse(null);
        }

        List<ProductDetailResponse.OptionGroupResponse> optionGroups = buildOptionGroups(product);

        List<String> imageUrls = getAllImageUrls(product.getId());

        Map<String, Object> reviewStatistics = reviewCoreService.findProductReviewStatistics(product.getId());
        Long totalReviewCount = (Long) reviewStatistics.get("totalReviewCount");
        Integer reviewCount = totalReviewCount != null ? totalReviewCount.intValue() : 0;

        return new ProductDetailResponse(
            product.getId(), product.getPlaceId(), placeName, product.getName(),
            product.getDescription(), imageUrls, product.getOriginalPrice(),
            product.getDiscountPrice(), product.getDiscountRate(), product.getRating(),
            reviewCount, product.getIsRepresentative(), product.getIsSoldOut(),
            categoryName, optionGroups
        );
    }

    private List<ProductDetailResponse.OptionGroupResponse> buildOptionGroups(Product product) {
        List<ProductDetailResponse.OptionGroupResponse> result = new ArrayList<>();

        List<ProductOptionGroup> productOptionGroups = productCoreService.findProductOptionGroupsByProductId(product.getId());
        if (!productOptionGroups.isEmpty()) {
            List<Long> optionGroupIds = productOptionGroups.stream()
                .map(ProductOptionGroup::getId)
                .toList();
            List<ProductOption> productOptions = productCoreService.findProductOptionsByOptionGroupIds(optionGroupIds);
            Map<Long, List<ProductOption>> optionsByGroupId = productOptions.stream()
                .collect(Collectors.groupingBy(ProductOption::getOptionGroupId));

            for (ProductOptionGroup group : productOptionGroups) {
                List<ProductDetailResponse.OptionResponse> options = optionsByGroupId
                    .getOrDefault(group.getId(), Collections.emptyList())
                    .stream()
                    .map(this::convertToOptionResponse)
                    .toList();

                result.add(new ProductDetailResponse.OptionGroupResponse(
                    group.getId(), group.getName(), group.getDescription(),
                    group.getIsRequired(), group.getIsMultipleSelect(),
                    group.getMinSelect(), group.getMaxSelect(), false, options
                ));
            }
        }

        List<ProductCommonOptionGroup> productCommonOptionGroups = productCoreService.findProductCommonOptionGroupsByProductId(product.getId());
        if (!productCommonOptionGroups.isEmpty()) {
            List<Long> commonOptionGroupIds = productCommonOptionGroups.stream()
                .map(ProductCommonOptionGroup::getId)
                .toList();
            List<ProductCommonOption> commonOptions = productCoreService.findProductCommonOptionsByOptionGroupIds(commonOptionGroupIds);
            Map<Long, List<ProductCommonOption>> commonOptionsByGroupId = commonOptions.stream()
                .collect(Collectors.groupingBy(ProductCommonOption::getOptionGroupId));

            for (ProductCommonOptionGroup group : productCommonOptionGroups) {
                List<ProductDetailResponse.OptionResponse> options = commonOptionsByGroupId
                    .getOrDefault(group.getId(), Collections.emptyList())
                    .stream()
                    .map(this::convertProductCommonOptionToOptionResponse)
                    .toList();

                result.add(new ProductDetailResponse.OptionGroupResponse(
                    group.getId(), group.getName(), group.getDescription(),
                    group.getIsRequired(), group.getIsMultipleSelect(),
                    group.getMinSelect(), group.getMaxSelect(), true, options
                ));
            }
        }

        return result;
    }

    private ProductDetailResponse.OptionResponse convertToOptionResponse(ProductOption option) {
        return new ProductDetailResponse.OptionResponse(
            option.getId(), option.getName(), option.getAdditionalPrice(), option.getIsSoldOut()
        );
    }

    private ProductDetailResponse.OptionResponse convertProductCommonOptionToOptionResponse(ProductCommonOption option) {
        return new ProductDetailResponse.OptionResponse(
            option.getId(), option.getName(), option.getAdditionalPrice(), option.getIsSoldOut()
        );
    }

    private String getFirstImageUrl(Long productId) {
        return productCoreService.getFirstImageUrl(productId);
    }

    private List<String> getAllImageUrls(Long productId) {
        return productCoreService.getAllImageUrls(productId);
    }

    @Transactional(readOnly = true)
    public ProductReviewsByRatingWithPagination getProductReviewsByRatingWithPagination(Long productId, int page, int size) {
        ReviewsByRatingResult result = reviewCoreService.findProductReviewsByRating(productId, page, size);

        Map<Integer, List<ProductReviewListItem>> reviewsByRating = result.getReviewsByRating().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry<Integer, List<LatestReviewListItemDto>>::getKey,
                        entry -> entry.getValue().stream()
                                .map(this::convertToProductReviewListItem)
                                .toList()
                ));

        List<ProductReviewListItem> allReviews = result.getAllReviews().stream()
                .map(this::convertToProductReviewListItem)
                .toList();

        ProductReviewsByRatingResponse response = new ProductReviewsByRatingResponse(
            reviewsByRating, allReviews, result.getTotalReviewCount()
        );

        return new ProductReviewsByRatingWithPagination(response, result.getTotalElements());
    }

    private ProductReviewListItem convertToProductReviewListItem(LatestReviewListItemDto dto) {
        return new ProductReviewListItem(
            dto.id(), dto.imageUrls(), dto.totalRating(), dto.content(),
            dto.memberNickname(), dto.memberProfileImageUrl(), dto.createdAt(),
            dto.productId(), dto.productName()
        );
    }

    @Transactional(readOnly = true)
    public ProductReviewStatisticsResponse getProductReviewStatistics(Long productId) {
        Map<String, Object> statistics = reviewCoreService.findProductReviewStatistics(productId);

        Product product = productCoreService.findProductById(productId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        return new ProductReviewStatisticsResponse(
            product.getRating(),
            (Long) statistics.get("totalReviewCount"),
            (Double) statistics.get("averageTasteRating"),
            (Double) statistics.get("averageAmountRating"),
            (Double) statistics.get("averagePriceRating")
        );
    }

}
