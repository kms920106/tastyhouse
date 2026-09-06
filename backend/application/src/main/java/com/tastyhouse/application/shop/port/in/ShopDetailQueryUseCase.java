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

@WebApp
public interface ShopDetailQueryUseCase {

    ShopDetailViewResult getShopDetail(Long shopId);

    ShopInfoViewResult getShopInfo(Long shopId);

    ShopNoticeResult getShopNotice(Long shopId);

    List<ShopBannerImageResult> getShopBanners(Long shopId);

    List<ShopProductCategoryViewResult> getShopProducts(Long shopId);

    List<ShopPhotoCategoryViewResult> getShopPhotos(Long shopId);

    ReviewsByRatingResult getShopReviewsByRatingWithPagination(Long shopId, int page, int size, Boolean hasImage, String sortType);

    ShopReviewStatisticsViewResult getShopReviewStatistics(Long shopId);

    List<PopularProductItemResult> getPopularProducts(Long shopId);

    boolean isBookmarked(Long shopId, Long memberId);
}
