package com.tastyhouse.webapplication.shop.port.in;

import java.util.List;

import com.tastyhouse.webapplication.shop.response.ShopBannerResponse;
import com.tastyhouse.webapplication.shop.response.ShopBookmarkResponse;
import com.tastyhouse.webapplication.shop.response.ShopDetailResponse;
import com.tastyhouse.webapplication.shop.response.ShopInfoResponse;
import com.tastyhouse.webapplication.shop.response.ShopNoticeResponse;
import com.tastyhouse.webapplication.shop.response.ShopPhotoCategoryResponse;
import com.tastyhouse.webapplication.shop.response.ShopPopularProductResponse;
import com.tastyhouse.webapplication.shop.response.ShopProductCategoryResponse;
import com.tastyhouse.webapplication.shop.response.ShopReviewStatisticsResponse;
import com.tastyhouse.webapplication.shop.response.ShopReviewsByRatingPageResponse;

/**
 * 가게 상세 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ShopDetailQueryUseCase {

    ShopDetailResponse getShopDetail(Long shopId);

    ShopInfoResponse getShopInfo(Long shopId);

    ShopNoticeResponse getShopNotice(Long shopId);

    List<ShopBannerResponse> getShopBanners(Long shopId);

    List<ShopProductCategoryResponse> getShopProducts(Long shopId);

    List<ShopPhotoCategoryResponse> getShopPhotos(Long shopId);

    ShopReviewsByRatingPageResponse getShopReviewsByRatingWithPagination(Long shopId, int page, int size, Boolean hasImage, String sortType);

    ShopReviewStatisticsResponse getShopReviewStatistics(Long shopId);

    List<ShopPopularProductResponse> getPopularProducts(Long shopId);

    ShopBookmarkResponse isBookmarked(Long shopId, Long memberId);
}
