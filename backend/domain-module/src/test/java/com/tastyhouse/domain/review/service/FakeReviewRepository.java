package com.tastyhouse.domain.review.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.repository.ReviewRepository;
import com.tastyhouse.domain.review.vo.ReviewId;

/**
 * 리뷰 write 포트의 인메모리 fake.
 *
 * <p>{@code save}가 신규 저장 시 <b>새 인스턴스를 반환</b>하는 것까지 실제 어댑터와 같게 재현한다
 * (다른 fake write 포트 선례와 동일한 이유 — {@link FakeReviewBlindRequestRepository} 참고).
 */
public class FakeReviewRepository implements ReviewRepository {

    private final Map<Long, Review> reviews = new HashMap<>();
    private long sequence = 0L;

    @Override
    public Optional<Review> findById(ReviewId reviewId) {
        return Optional.ofNullable(reviews.get(reviewId.value()));
    }

    @Override
    public Optional<Review> findByIdAndMemberId(ReviewId reviewId, MemberId memberId) {
        return findById(reviewId).filter(review -> review.getMemberId().equals(memberId));
    }

    @Override
    public boolean existsByOrderIdAndProductId(OrderId orderId, ProductId productId) {
        return reviews.values().stream().anyMatch(review ->
            Objects.equals(review.getOrderId(), orderId) && review.getProductId().equals(productId));
    }

    @Override
    public Review save(Review review) {
        if (review.getId() != null) {
            reviews.put(review.getId(), review);
            return review;
        }

        Review persisted = Review.reconstitute(
            ++sequence,
            review.getShopId(),
            review.getProductId(),
            review.getMemberId(),
            review.getContent(),
            review.getTotalRating(),
            review.getTasteRating(),
            review.getAmountRating(),
            review.getPriceRating(),
            review.getAtmosphereRating(),
            review.getKindnessRating(),
            review.getHygieneRating(),
            review.isWillRevisit(),
            review.getOrderId(),
            review.isHidden(),
            review.isOwnerOnly(),
            null
        );
        reviews.put(persisted.getId(), persisted);
        return persisted;
    }

    @Override
    public void deleteById(ReviewId reviewId) {
        reviews.remove(reviewId.value());
    }
}
