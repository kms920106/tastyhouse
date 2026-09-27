package com.tastyhouse.application.review.store;

import java.util.Optional;

import com.tastyhouse.application.review.port.out.write.ReviewStatePort;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.vo.ReviewId;

public class ReviewStore implements ReviewRepository {
    private final ReviewStatePort reviewStatePort;

    public ReviewStore(ReviewStatePort reviewStatePort) {
        this.reviewStatePort = reviewStatePort;
    }

    @Override
    public Optional<Review> findById(ReviewId reviewId) {
        return reviewStatePort.findById(reviewId.value()).map(ReviewStateMapper::toDomain);
    }

    @Override
    public Optional<Review> findByIdAndMemberId(ReviewId reviewId, MemberId memberId) {
        return reviewStatePort.findByIdAndMemberId(reviewId.value(), memberId.value())
            .map(ReviewStateMapper::toDomain);
    }

    @Override
    public boolean existsByOrderIdAndProductId(OrderId orderId, ProductId productId) {
        return reviewStatePort.existsByOrderIdAndProductId(orderId.value(), productId.value());
    }

    @Override
    public Review save(Review review) {
        return ReviewStateMapper.toDomain(reviewStatePort.save(ReviewStateMapper.toState(review)));
    }

    @Override
    public void deleteById(ReviewId reviewId) {
        reviewStatePort.deleteById(reviewId.value());
    }
}
