package com.tastyhouse.webapplication.product.service;

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
import com.tastyhouse.webapplication.product.port.out.ProductBatchItemView;
import com.tastyhouse.webapplication.product.port.out.ProductDetailView;
import com.tastyhouse.webapplication.product.port.out.ProductPriceView;
import com.tastyhouse.webapplication.product.port.out.ProductReviewStatisticsView;
import com.tastyhouse.webapplication.product.port.in.ProductBatchQuery;
import com.tastyhouse.webapplication.product.port.in.ProductQueryUseCase;

/**
 * 회원용 상품 조회 서비스. infrastructure의 read 어댑터 {@link ProductQueryPort}만 주입하고, 조회 결과를
 * 읽기 계약으로 반환한다. web-api에는 상품 command 경로가 없어 QueryService만 둔다.
 *
 * <p><b>챕터 10 이후 Response를 조립하지 않는다</b> — Response record가 web-api로 승격됐으므로 표현
 * 조립은 컨트롤러의 몫이다. 대신 <b>계산은 전부 이 계층에 남는다</b>: 주문유형별 가격 해석
 * ({@code ProductPrice#resolvePrice}), 메뉴 평가 수의 null 기본값, 리뷰 통계의 두 출처 병합. 그래서
 * 공용 읽기 계약으로 표현되지 않는 값은 앱 네임스페이스의 {@code port.out} View로 나른다.
 *
 * <p>상품 화면이 곁들여 보여주는 리뷰 통계·평점대별 목록은 review 도메인의
 * {@link ReviewQueryPort}·{@link ReviewStatisticsQueryPort}를 직접 주입해 조회한다 — 이 조회들은 상품 화면
 * 전용이라 리뷰 쪽에는 다른 호출부가 없었고, review QueryService를 경유하면 그쪽이 상품 정보를 얻기 위해 이
 * 서비스를 다시 주입해야 해서 빈 순환 참조가 생긴다. 표현 목적 조회는 DAO 계층에서 교차하는 것이 옳다.
 */
@Service
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

    /**
     * 메뉴 상세를 조회한다. {@code orderMethod}로 가격 행의 채널 가격을 <b>서버가 해석해</b> 내려준다.
     *
     * <p>기존 {@code originalPrice}·{@code discountPrice}·{@code discountRate} 필드는 그대로 유지한다 —
     * 여러 화면이 읽는 계약이고, 가격 행이 하나뿐인 메뉴(대부분)는 {@code PRODUCT.original_price}가
     * {@code sort=0} 행의 배달가와 동기화돼 있어 기존 동작이 완전히 같다.
     *
     * <p>가격 행이 없는 메뉴(이관 이전 데이터)는 예외를 던지지 않고 빈 목록을 준다 — 가격 행 도입 전에
     * 등록된 메뉴의 상세가 500으로 막히면 그 메뉴는 아예 팔 수 없게 된다.
     */
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

    /**
     * 가격 행 하나를 주문유형으로 해석된 단일 가격으로 접는다.
     *
     * <p>해석 규칙을 이 서비스가 다시 쓰지 않고 도메인 모델 {@code ProductPrice#resolvePrice}에 위임한다 —
     * 주문 생성 경로가 같은 메서드로 결제 금액을 정하므로, 여기서 규칙을 복제하면 표시 가격과 결제
     * 금액이 갈릴 수 있다. 그러기 위해 read model을 도메인 모델로 되짚어 올린다({@code reconstitute}).
     *
     * <p>매장가는 응답에 담지 않는다 — 표시 전용 값이라 손님 계약에 없다.
     */
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

    /**
     * 상품 배치 조회. (상품ID, 옵션ID) 조합 목록을 받아 상품 단위로 그룹핑하여 반환합니다.
     * 판매 종료/미존재 상품은 제외하지 않고 available=false 로 남기며, 옵션은 조회에 성공한 것만 포함됩니다.
     *
     * <p><b>가격 행({@code prices})을 함께 내려준다</b> — 장바구니는 담을 때 고른 {@code priceId}만
     * 보관하므로, 그 값으로 가격명·가격을 되찾을 수 있어야 "곱빼기 13,000원"으로 담은 항목이 화면과
     * 결제 금액에 반영된다. 가격 해석은 상세 조회와 같은 규칙({@code ProductPrice#resolvePrice})을 쓰며
     * 요청의 {@code orderMethod}가 그 기준이다.
     *
     * <p>가격 행 조회는 메뉴별로 부르지 않고 한 번에 읽어 {@code productId}로 그룹핑한다 — 장바구니
     * 항목 수만큼 쿼리가 나가는 N+1을 피한다.
     */
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

    /**
     * 배치 조회 대상 메뉴들의 가격 행을 한 번에 읽어 {@code productId}별로 묶는다.
     *
     * <p>{@code available=false}(판매 종료·미존재) 상품은 조회 대상에서 빼 불필요한 조회를 줄인다 —
     * 그 상품은 응답의 다른 필드도 비어 있어 화면이 "판매 종료"로만 다룬다.
     */
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

    /**
     * 상품 리뷰 통계 — {@code totalRating}만 <b>상품 투영</b>에서 오고 나머지는 리뷰 통계에서 온다.
     *
     * <p>두 출처를 합치는 판단이므로 표현 계층에 맡기지 않고 여기서 끝낸다
     * ({@link ProductReviewStatisticsView}의 Javadoc 참조).
     */
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

    /**
     * 상품 검색(통합검색) — search 도메인이 메뉴 검색 결과를 조립할 때 사용한다.
     */
    public PageResult<SearchProductItemResult> searchByKeyword(String keyword, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return productQueryPort.searchByKeyword(keyword, pageQuery);
    }

    /**
     * 가게 상세 화면의 상품 목록 — shop 도메인이 카테고리별로 그룹핑해 조립한다.
     */
    public List<ShopProductItemResult> findShopProducts(Long shopId) {
        return productQueryPort.findShopProducts(shopId);
    }

    /**
     * 가게 상세 상단 인기 메뉴 그룹 — shop 도메인이 응답을 조립한다.
     *
     * <p>사장님 추천 우선 채우기와 판매량 순위 조합은 DAO가 소유한다({@code ProductQueryPort#findPopularProducts}) —
     * 두 갈래를 각각 조회해 이어 붙이는 규칙이 SQL 인접 계층에 있어야 인덱스·집계 창을 한눈에 검토할 수 있다.
     */
    public List<PopularProductItemResult> findPopularProducts(Long shopId) {
        return productQueryPort.findPopularProducts(shopId);
    }

    /**
     * 가게의 노출 상품 카테고리 목록 — shop 도메인이 카테고리별 상품 묶음을 조립할 때 사용한다.
     */
    public List<ProductCategoryResult> findShopProductCategories(Long shopId) {
        return productQueryPort.findProductCategories(shopId);
    }

    private ProductDetailResult loadProductDetail(Long productId) {
        return productQueryPort.findProductDetailById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    /**
     * 상품의 리뷰 평점 통계. 리뷰가 없으면 평균값은 모두 null이고 건수만 0이다.
     */
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

    /**
     * 상품의 평점대별 리뷰 묶음 — 평점 1~5 각각의 상위 5건과 전체 페이징 목록을 함께 조회한다.
     */
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
