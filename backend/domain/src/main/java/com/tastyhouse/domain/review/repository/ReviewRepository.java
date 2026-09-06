package com.tastyhouse.domain.review.repository;

import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.vo.ReviewId;

public interface ReviewRepository {
    Optional<Review> findById(ReviewId reviewId);

    Optional<Review> findByIdAndMemberId(ReviewId reviewId, MemberId memberId);

    boolean existsByOrderIdAndProductId(OrderId orderId, ProductId productId);

    Review save(Review review);

    void deleteById(ReviewId reviewId);
}
