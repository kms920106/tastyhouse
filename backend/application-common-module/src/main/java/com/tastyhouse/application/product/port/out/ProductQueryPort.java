package com.tastyhouse.application.product.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 상품 조회 포트(CQRS query 측 아웃바운드 포트) — 회원 화면용.
 *
 * <p>가게 상세의 메뉴판·상품 상세·검색·할인 목록처럼 회원에게 상품을 노출하는 조회를 담당한다.
 * 관리 화면 조회는 {@link ProductManagementQueryPort}(관리자)·
 * {@code ProductOwnerQueryPort}(점주)가 소유한다.
 *
 * <p>분할 전 이 포트는 35개 메서드에 세 앱의 조회가 모두 섞여 있었다. 실측 결과 앱마다 쓰는 메서드가
 * 거의 겹치지 않았고(회원 12 · 관리자 5 · 점주 15), 겹치는 6개만 <b>공유 메서드</b>로 남아 각 포트에
 * 선언만 중복한다. 구현은 {@code ProductQueryDao} 하나가 담당하므로 투영 코드는 복제되지 않는다.
 */
public interface ProductQueryPort {

    PageResult<TodayDiscountProductResult> findTodayDiscountProducts(PageQuery pageQuery);

    PageResult<SearchProductItemResult> searchByKeyword(String keyword, PageQuery pageQuery);

    List<ProductBatchResult> findProductsBatch(List<ProductBatchItem> items);

    List<ShopProductItemResult> findShopProducts(Long shopId);

    List<ProductPriceResult> findProductPrices(Long productId);

    List<ProductPriceResult> findProductPricesByProductIds(List<Long> productIds);

    List<ProductPriceResult> findShopProductPrices(Long shopId);

    long countVisibleProducts(Long shopId);

    List<PopularProductItemResult> findPopularProducts(Long shopId);

    /** 공유 메서드 — {@link ProductManagementQueryPort}에도 같은 시그니처로 선언돼 있다. */
    ProductOptionsResult findProductOptions(Long productId);

    /** 공유 메서드 — {@link ProductManagementQueryPort}에도 같은 시그니처로 선언돼 있다. */
    List<String> findProductImageUrls(Long productId);

    /** 공유 메서드 — {@link ProductManagementQueryPort}에도 같은 시그니처로 선언돼 있다. */
    Optional<ProductDetailResult> findProductDetailById(Long productId);

    /** 공유 메서드 — {@link ProductManagementQueryPort}에도 같은 시그니처로 선언돼 있다. */
    List<ProductCategoryResult> findProductCategories(Long shopId);

    /** 공유 메서드 — {@code ProductOwnerQueryPort}에도 같은 시그니처로 선언돼 있다. */
    Optional<ProductNutritionResult> findNutrition(Long productId);

    /** 공유 메서드 — {@code ProductOwnerQueryPort}에도 같은 시그니처로 선언돼 있다. */
    List<String> findAllergenTypes(Long productId);
}
