package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDate;
import java.util.List;

import com.tastyhouse.application.review.port.out.ShopReviewDetailViewResult;
import com.tastyhouse.application.review.port.out.ShopReviewListItemViewResult;
import com.tastyhouse.application.review.port.out.ShopReviewSortTypeView;
import com.tastyhouse.application.review.port.out.ShopReviewStatisticsOwnerResult;
import com.tastyhouse.application.review.port.out.ReviewBlindReasonView;
import com.tastyhouse.domain.shared.page.PageResult;

@CeoApp
public interface ShopReviewQueryUseCase {

    PageResult<ShopReviewListItemViewResult> getReviews(
        Long ceoId,
        Long shopId,
        String tab,
        LocalDate startDate,
        LocalDate endDate,
        Integer rating,
        String orderMethod,
        Boolean hasImage,
        String sortType,
        int page,
        int size
    );

    ShopReviewDetailViewResult getReviewDetail(Long ceoId, Long shopId, Long reviewId);

    ShopReviewStatisticsOwnerResult getStatistics(Long ceoId, Long shopId);

    ShopReviewSortTypeView getSortType(Long ceoId, Long shopId);

    List<ReviewBlindReasonView> getBlindReasons();
}
