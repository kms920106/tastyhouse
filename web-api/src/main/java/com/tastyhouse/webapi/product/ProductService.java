package com.tastyhouse.webapi.product;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.domain.product.domain.vo.ProductId;
import com.tastyhouse.core.domain.product.application.ProductQueryService;
import com.tastyhouse.core.domain.product.application.dto.command.BatchItem;
import com.tastyhouse.core.domain.product.application.dto.command.ProductBatchQuery;
import com.tastyhouse.core.domain.product.application.dto.result.ProductBatchResult;
import com.tastyhouse.core.domain.product.application.dto.result.ProductOptionsResult;
import com.tastyhouse.core.domain.product.application.dto.result.TodayDiscountProductResult;
import com.tastyhouse.core.domain.review.application.ReviewQueryService;
import com.tastyhouse.core.domain.review.application.dto.result.LatestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.ProductReviewStatisticsResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewsByRatingResult;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.webapi.file.FileService;
import com.tastyhouse.webapi.common.PaginationResponse;
import com.tastyhouse.webapi.product.request.ProductBatchRequest;
import com.tastyhouse.webapi.product.response.ProductBatchOptionResponse;
import com.tastyhouse.webapi.product.response.ProductBatchResponse;
import com.tastyhouse.webapi.product.response.ProductDetailResponse;
import com.tastyhouse.webapi.product.response.ProductImagesResponse;
import com.tastyhouse.webapi.product.response.ProductOptionGroupResponse;
import com.tastyhouse.webapi.product.response.ProductOptionGroupsResponse;
import com.tastyhouse.webapi.product.response.ProductOptionResponse;
import com.tastyhouse.webapi.product.response.ProductResponse;
import com.tastyhouse.webapi.product.response.ProductReviewCountResponse;
import com.tastyhouse.webapi.product.response.ProductReviewListItemResponse;
import com.tastyhouse.webapi.product.response.ProductReviewStatisticsResponse;
import com.tastyhouse.webapi.product.response.ProductReviewsByRatingPageResponse;
import com.tastyhouse.webapi.product.response.ProductReviewsByRatingResponse;
import com.tastyhouse.webapi.product.response.ProductTodayDiscountListItemResponse;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductQueryService productQueryService;
    private final ReviewQueryService reviewQueryService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public PaginationResponse<ProductTodayDiscountListItemResponse> searchTodayDiscountProducts(int page, int size) {
        PageResult<ProductTodayDiscountListItemResponse> pageResult = productQueryService.findTodayDiscountProducts(page, size)
            .map(this::convertToTodayDiscountProductListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    private ProductTodayDiscountListItemResponse convertToTodayDiscountProductListItemResponse(TodayDiscountProductResult dto) {
        return ProductTodayDiscountListItemResponse.from(
            dto.id(),
            dto.shopName(),
            dto.name(),
            fileService.getUrlByPath(dto.imageUrl()),
            dto.originalPrice(),
            dto.discountPrice(),
            dto.discountRate()
        );
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse findProductById(Long productId) {
        Product product = productQueryService.findProductById(ProductId.of(productId))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductDetailResponse.from(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getOriginalPrice(),
            product.getDiscountPrice(),
            product.getDiscountRate(),
            product.isSoldOut()
        );
    }

    @Transactional(readOnly = true)
    public ProductReviewCountResponse findProductReviewCount(Long productId) {
        productQueryService.findProductById(ProductId.of(productId))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        ProductReviewStatisticsResult statistics = reviewQueryService.findProductReviewStatistics(productId);
        Long total = statistics.totalReviewCount();
        return ProductReviewCountResponse.from(total != null ? total.intValue() : 0);
    }

    @Transactional(readOnly = true)
    public ProductOptionGroupsResponse findProductOptions(Long productId) {
        productQueryService.findProductById(ProductId.of(productId))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        ProductOptionsResult result = productQueryService.findProductOptions(productId);
        return ProductOptionGroupsResponse.from(convertToOptionGroupResponses(result));
    }

    /**
     * 상품 배치 조회. (상품ID, 옵션ID) 조합 목록을 받아 상품 단위로 그룹핑하여 반환합니다.
     * 판매 종료/미존재 상품은 제외하지 않고 available=false 로 남기며, 옵션은 조회에 성공한 것만 포함됩니다.
     */
    @Transactional(readOnly = true)
    public ProductBatchResponse findProductsBatch(ProductBatchRequest request) {
        List<BatchItem> items = request.items().stream()
            .map(item -> BatchItem.of(item.productId(), item.optionId()))
            .toList();

        List<ProductBatchResult> results = productQueryService.findProductsBatch(ProductBatchQuery.of(items));

        List<ProductResponse> products = results.stream()
            .map(this::convertToProductBatchResponse)
            .toList();

        return ProductBatchResponse.from(products);
    }

    private ProductResponse convertToProductBatchResponse(ProductBatchResult result) {
        List<ProductBatchOptionResponse> options = result.options().stream()
            .map(option -> ProductBatchOptionResponse.from(
                option.id(), option.name(), option.price()
            ))
            .toList();
        return ProductResponse.from(
            result.id(),
            result.available(),
            result.name(),
            fileService.getUrlByPath(result.imageFilePath()),
            result.originalPrice(),
            result.discountPrice(),
            options
        );
    }

    private List<ProductOptionGroupResponse> convertToOptionGroupResponses(ProductOptionsResult result) {
        return result.optionGroups().stream()
            .map(group -> {
                List<ProductOptionResponse> options = group.options().stream()
                    .map(o -> ProductOptionResponse.from(
                        o.id(), o.name(), o.additionalPrice(), o.soldOut()))
                    .toList();
                return ProductOptionGroupResponse.from(
                    group.id(), group.name(), group.description(),
                    group.required(), group.multipleSelect(),
                    group.minSelect(), group.maxSelect(), group.common(), options
                );
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public ProductImagesResponse findProductImages(Long productId) {
        productQueryService.findProductById(ProductId.of(productId))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        List<String> imageUrls = productQueryService.getAllImageFilePaths(productId).stream()
            .map(fileService::getUrlByPath)
            .toList();
        return ProductImagesResponse.from(imageUrls);
    }

    @Transactional(readOnly = true)
    public ProductReviewsByRatingPageResponse getProductReviewsByRatingWithPagination(Long productId, int page, int size, Boolean hasImage) {
        ReviewsByRatingResult result = reviewQueryService.findProductReviewsByRating(productId, page, size, hasImage);

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
            reviewsByRating, allReviews, result.getTotalReviewCount()
        );

        return new ProductReviewsByRatingPageResponse(response, result.getTotalElements());
    }

    private ProductReviewListItemResponse convertToProductReviewListItemResponse(LatestReviewListItemResult dto) {
        return ProductReviewListItemResponse.from(
            dto.id(),
            dto.imageUrls().stream().map(fileService::getUrlByPath).toList(),
            dto.totalRating(),
            dto.content(),
            dto.memberId().value(),
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

        Product product = productQueryService.findProductById(ProductId.of(productId))
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
