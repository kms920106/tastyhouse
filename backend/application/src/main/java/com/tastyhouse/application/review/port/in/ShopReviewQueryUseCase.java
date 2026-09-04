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

/**
 * 가게 리뷰 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopReviewQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
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
