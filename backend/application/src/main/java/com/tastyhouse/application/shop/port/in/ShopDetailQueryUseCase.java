package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import com.tastyhouse.application.review.port.out.ReviewsByRatingResult;
import com.tastyhouse.application.shop.port.out.ShopBannerImageResult;
import com.tastyhouse.application.shop.port.out.ShopNoticeResult;
import com.tastyhouse.application.product.port.out.PopularProductItemResult;
import com.tastyhouse.application.shop.port.out.ShopDetailViewResult;
import com.tastyhouse.application.shop.port.out.ShopInfoViewResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryViewResult;
import com.tastyhouse.application.shop.port.out.ShopProductCategoryViewResult;
import com.tastyhouse.application.shop.port.out.ShopReviewStatisticsViewResult;

/**
 * 가게 상세 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p><b>챕터 10</b> 이후 이 포트는 Response가 아니라 읽기 계약을 반환한다 — Swagger·HTTP 표현은
 * web-api의 {@code ..adapter.in.web.response..}가 소유하고, 이 계층은 조립에 필요한 값만 돌려준다.
 */
@WebApp
public interface ShopDetailQueryUseCase {

    ShopDetailViewResult getShopDetail(Long shopId);

    ShopInfoViewResult getShopInfo(Long shopId);

    /** 노출 중인 공지가 없으면 {@code null} — 대부분의 가게에 공지가 없어 404를 쓰지 않는다. */
    ShopNoticeResult getShopNotice(Long shopId);

    List<ShopBannerImageResult> getShopBanners(Long shopId);

    List<ShopProductCategoryViewResult> getShopProducts(Long shopId);

    List<ShopPhotoCategoryViewResult> getShopPhotos(Long shopId);

    ReviewsByRatingResult getShopReviewsByRatingWithPagination(Long shopId, int page, int size, Boolean hasImage, String sortType);

    ShopReviewStatisticsViewResult getShopReviewStatistics(Long shopId);

    List<PopularProductItemResult> getPopularProducts(Long shopId);

    boolean isBookmarked(Long shopId, Long memberId);
}
