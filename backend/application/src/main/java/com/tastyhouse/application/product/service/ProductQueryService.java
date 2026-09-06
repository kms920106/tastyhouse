package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.menureview.port.out.MenuReviewStatisticsQueryPort;
import com.tastyhouse.application.product.port.out.PopularProductItemResult;
import com.tastyhouse.application.product.port.out.ProductBatchItem;
import com.tastyhouse.application.product.port.out.ProductBatchResult;
import com.tastyhouse.application.product.port.out.ProductCategoryResult;
import com.tastyhouse.application.product.port.out.ProductDetailResult;
import com.tastyhouse.application.product.port.out.ProductOptionsResult;
import com.tastyhouse.application.product.port.out.ProductPriceResult;
import com.tastyhouse.application.product.port.out.ProductQueryPort;
import com.tastyhouse.application.product.port.out.SearchProductItemResult;
import com.tastyhouse.application.product.port.out.ShopProductItemResult;
import com.tastyhouse.application.product.port.out.TodayDiscountProductResult;
import com.tastyhouse.application.review.port.out.LatestReviewListItemResult;
import com.tastyhouse.application.review.port.out.ProductReviewStatisticsResult;
import com.tastyhouse.application.review.port.out.ReviewQueryPort;
import com.tastyhouse.application.review.port.out.ReviewStatisticsQueryPort;
import com.tastyhouse.application.review.port.out.ReviewsByRatingResult;
import com.tastyhouse.application.product.port.out.ProductBatchItemView;
import com.tastyhouse.application.product.port.out.ProductDetailView;
import com.tastyhouse.application.product.port.out.ProductPriceView;
import com.tastyhouse.application.product.port.out.ProductReviewStatisticsView;
import com.tastyhouse.application.product.port.in.ProductBatchQuery;
import com.tastyhouse.application.product.port.in.ProductQueryUseCase;

@Service
@WebApp
@Transactional(readOnly = true)
public class ProductQueryService implements ProductQueryUseCase {

    private final ProductQueryPort productQueryPort;
    private final ReviewQueryPort reviewQueryPort;
    private final ReviewStatisticsQueryPort reviewStatisticsQueryPort;
    private final MenuReviewStatisticsQueryPort menuReviewStatisticsQueryPort;

    public ProductQueryService(
        ProductQueryPort productQueryPort,
        ReviewQueryPort reviewQueryPort,
        ReviewStatisticsQueryPort reviewStatisticsQueryPort,
        MenuReviewStatisticsQueryPort menuReviewStatisticsQueryPort
    ) {
        this.productQueryPort = productQueryPort;
        this.reviewQueryPort = reviewQueryPort;
        this.reviewStatisticsQueryPort = reviewStatisticsQueryPort;
        this.menuReviewStatisticsQueryPort = menuReviewStatisticsQueryPort;
    }

    @Override
    public PageResult<TodayDiscountProductResult> searchTodayDiscountProducts(int page, int size) {
        return productQueryPort.findTodayDiscountProducts(PageQuery.of(page, size));
    }

    @Override
    public ProductDetailView findProductById(Long productId, String orderMethod) {
        ProductDetailResult dto = loadProductDetail(productId);
        Long menuReviewCount = menuReviewStatisticsQueryPort.countVisibleByProductId(productId);
        OrderMethod resolvedOrderMethod = OrderMethod.from(orderMethod);
        List<ProductPriceView> prices = productQueryPort.findProductPrices(productId).stream()
            .map(price -> toProductPriceView(price, resolvedOrderMethod))
            .toList();
        return new ProductDetailView(
            dto.id(),
            dto.name(),
            dto.description(),
            dto.originalPrice(),
            dto.discountPrice(),
            dto.discountRate(),
            dto.soldOut(),
            dto.weightText(),
            menuReviewCount != null ? menuReviewCount : 0L,
            prices
        );
    }

    private ProductPriceView toProductPriceView(ProductPriceResult dto, OrderMethod orderMethod) {
        ProductPrice price = ProductPrice.reconstitute(
            dto.id(),
            ProductId.of(dto.productId()),
            dto.priceName(),
            dto.deliveryPrice(),
            dto.storePrice(),
            dto.pickupPrice(),
            dto.sort(),
            dto.pickupPriceSetAt(),
            null,
            null
        );
        return new ProductPriceView(dto.id(), price.getPriceName(), price.resolvePrice(orderMethod));
    }

    @Override
    public int findProductReviewCount(Long productId) {
        loadProductDetail(productId);
        ProductReviewStatisticsResult statistics = findProductReviewStatistics(productId);
        Long total = statistics.totalReviewCount();
        return total != null ? total.intValue() : 0;
    }

    @Override
    public ProductOptionsResult findProductOptions(Long productId) {
        loadProductDetail(productId);
        return productQueryPort.findProductOptions(productId);
    }

    @Override
    public List<ProductBatchItemView> findProductsBatch(ProductBatchQuery query) {
        List<ProductBatchItem> items = query.items().stream()
            .map(item -> ProductBatchItem.of(item.productId(), item.optionId()))
            .toList();

        OrderMethod orderMethod = OrderMethod.from(query.orderMethod());
        List<ProductBatchResult> results = productQueryPort.findProductsBatch(items);
        Map<Long, List<ProductPriceView>> pricesByProductId =
            findBatchPricesByProductId(results, orderMethod);

        return results.stream()
            .map(result -> toProductBatchItemView(
                result,
                pricesByProductId.getOrDefault(result.id(), List.of())
            ))
            .toList();
    }

    private Map<Long, List<ProductPriceView>> findBatchPricesByProductId(
        List<ProductBatchResult> results,
        OrderMethod orderMethod
    ) {
        List<Long> productIds = results.stream()
            .filter(ProductBatchResult::available)
            .map(ProductBatchResult::id)
            .distinct()
            .toList();

        return productQueryPort.findProductPricesByProductIds(productIds).stream()
            .collect(Collectors.groupingBy(
                ProductPriceResult::productId,
                LinkedHashMap::new,
                Collectors.mapping(price -> toProductPriceView(price, orderMethod), Collectors.toList())
            ));
    }

    private ProductBatchItemView toProductBatchItemView(
        ProductBatchResult result,
        List<ProductPriceView> prices
    ) {
        return new ProductBatchItemView(
            result.id(),
            result.available(),
            result.name(),
            result.imageUrl(),
            result.originalPrice(),
            result.discountPrice(),
            result.options(),
            prices
        );
    }

    @Override
    public List<String> findProductImages(Long productId) {
        loadProductDetail(productId);
        return productQueryPort.findProductImageUrls(productId);
    }

    @Override
    public ReviewsByRatingResult getProductReviewsByRatingWithPagination(
        Long productId,
        int page,
        int size,
        Boolean hasImage
    ) {
        return findProductReviewsByRating(productId, page, size, hasImage);
    }

    @Override
    public ProductReviewStatisticsView getProductReviewStatistics(Long productId) {
        ProductReviewStatisticsResult statistics = findProductReviewStatistics(productId);
        ProductDetailResult product = loadProductDetail(productId);

        return new ProductReviewStatisticsView(
            product.rating(),
            statistics.totalReviewCount(),
            statistics.averageTasteRating(),
            statistics.averageAmountRating(),
            statistics.averagePriceRating()
        );
    }

    public PageResult<SearchProductItemResult> searchByKeyword(String keyword, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return productQueryPort.searchByKeyword(keyword, pageQuery);
    }

    public List<ShopProductItemResult> findShopProducts(Long shopId) {
        return productQueryPort.findShopProducts(shopId);
    }

    public List<PopularProductItemResult> findPopularProducts(Long shopId) {
        return productQueryPort.findPopularProducts(shopId);
    }

    public List<ProductCategoryResult> findShopProductCategories(Long shopId) {
        return productQueryPort.findProductCategories(shopId);
    }

    private ProductDetailResult loadProductDetail(Long productId) {
        return productQueryPort.findProductDetailById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private ProductReviewStatisticsResult findProductReviewStatistics(Long productId) {
        Long totalCount = reviewStatisticsQueryPort.countVisibleByProductId(productId);

        if (totalCount > 0) {
            return new ProductReviewStatisticsResult(
                totalCount,
                reviewStatisticsQueryPort.getAverageTasteRatingByProductId(productId),
                reviewStatisticsQueryPort.getAverageAmountRatingByProductId(productId),
                reviewStatisticsQueryPort.getAveragePriceRatingByProductId(productId)
            );
        }

        return new ProductReviewStatisticsResult(totalCount, null, null, null);
    }

    private ReviewsByRatingResult findProductReviewsByRating(Long productId, int page, int size, Boolean hasImage) {
        Map<Integer, List<LatestReviewListItemResult>> reviewsByRating = new HashMap<>();
        for (int rating = 1; rating <= 5; rating++) {
            reviewsByRating.put(rating, reviewQueryPort.findReviewsByProductIdAndRating(productId, rating, 5));
        }

        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<LatestReviewListItemResult> allReviewsPage =
            reviewQueryPort.findLatestReviewsByProductId(productId, null, pageQuery, hasImage, ReviewSortType.LATEST);

        Long totalReviewCount = reviewStatisticsQueryPort.countVisibleByProductId(productId);

        return new ReviewsByRatingResult(
            reviewsByRating,
            allReviewsPage.content(),
            totalReviewCount,
            allReviewsPage.totalElements(),
            allReviewsPage.totalPages(),
            allReviewsPage.page(),
            allReviewsPage.size()
        );
    }
}
