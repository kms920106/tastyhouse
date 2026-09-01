package com.tastyhouse.webapplication.product.port.in;

import java.util.List;

import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.product.port.out.ProductOptionsResult;
import com.tastyhouse.application.product.port.out.TodayDiscountProductResult;
import com.tastyhouse.application.review.port.out.ReviewsByRatingResult;
import com.tastyhouse.webapplication.product.port.out.ProductBatchItemView;
import com.tastyhouse.webapplication.product.port.out.ProductDetailView;
import com.tastyhouse.webapplication.product.port.out.ProductReviewStatisticsView;

/**
 * 상품 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ProductQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p><b>챕터 10</b>에서 반환 타입이 Response에서 읽기 계약으로 바뀌었다 — Response record가 web-api로
 * 승격됐으므로 표현 조립은 컨트롤러가 담당한다. 페이징은 {@code PageResult}로 반환하고 컨트롤러가
 * {@code PaginationResponse.from(...)}으로 감싼다.
 */
public interface ProductQueryUseCase {

    PageResult<TodayDiscountProductResult> searchTodayDiscountProducts(int page, int size);

    ProductDetailView findProductById(Long productId, String orderMethod);

    /** 노출 리뷰 수. 리뷰가 없으면 0이다. */
    int findProductReviewCount(Long productId);

    ProductOptionsResult findProductOptions(Long productId);

    List<ProductBatchItemView> findProductsBatch(ProductBatchQuery query);

    /** 상품 이미지 URL 목록. 없으면 빈 목록이다. */
    List<String> findProductImages(Long productId);

    ReviewsByRatingResult getProductReviewsByRatingWithPagination(Long productId, int page, int size, Boolean hasImage);

    ProductReviewStatisticsView getProductReviewStatistics(Long productId);
}
