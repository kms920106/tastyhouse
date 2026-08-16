package com.tastyhouse.infrastructure.product.persistence;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.product.port.ProductReviewStatisticsPort;
import com.tastyhouse.infrastructure.review.query.ReviewStatisticsQueryDao;

/**
 * 상품 리뷰 통계 조회 포트({@link ProductReviewStatisticsPort}) 어댑터.
 *
 * <p>리뷰 집계 조회 자체는 리뷰 도메인 소유이므로 {@link ReviewStatisticsQueryDao}에 두고, 이 어댑터는 상품 도메인이
 * 필요로 하는 메서드만 골라 위임한다({@code rank}의 {@code MemberReviewCountAdapter}와 같은 형태).
 * 덕분에 상품 쪽 코드는 리뷰 도메인의 read model이나 QueryDSL을 알지 않는다.
 */
@Component
public class ProductReviewStatisticsAdapter implements ProductReviewStatisticsPort {

    private final ReviewStatisticsQueryDao reviewStatisticsQueryDao;

    public ProductReviewStatisticsAdapter(ReviewStatisticsQueryDao reviewStatisticsQueryDao) {
        this.reviewStatisticsQueryDao = reviewStatisticsQueryDao;
    }

    @Override
    public Long countVisibleReviewsByProductId(Long productId) {
        return reviewStatisticsQueryDao.countVisibleByProductId(productId);
    }

    @Override
    public Double getAverageTasteRatingByProductId(Long productId) {
        return reviewStatisticsQueryDao.getAverageTasteRatingByProductId(productId);
    }

    @Override
    public Double getAverageAmountRatingByProductId(Long productId) {
        return reviewStatisticsQueryDao.getAverageAmountRatingByProductId(productId);
    }

    @Override
    public Double getAveragePriceRatingByProductId(Long productId) {
        return reviewStatisticsQueryDao.getAveragePriceRatingByProductId(productId);
    }
}
