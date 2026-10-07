package com.tastyhouse.application.review.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.in.ReviewDetailQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewDetailResult;
import com.tastyhouse.application.review.port.out.ReviewDetailView;

@Service
@Transactional(readOnly = true)
class ReviewDetailQueryService implements ReviewDetailQueryUseCase {

    private final ReviewDetailReader reviewDetailReader;

    public ReviewDetailQueryService(ReviewDetailReader reviewDetailReader) {
        this.reviewDetailReader = reviewDetailReader;
    }

    @Override
    public Optional<ReviewDetailView> findReviewDetail(Long reviewId, Long viewerMemberId) {
        return reviewDetailReader.findReviewDetailResult(ReviewId.of(reviewId), viewerMemberId)
            .map(result -> toReviewDetailView(result, viewerMemberId));
    }

    private ReviewDetailView toReviewDetailView(ReviewDetailResult dto, Long viewerMemberId) {
        boolean author = viewerMemberId != null && viewerMemberId.equals(dto.memberId());
        String orderMethod = author ? dto.orderMethod() : null;

        return new ReviewDetailView(
            dto.id(),
            dto.shopId(),
            dto.shopName(),
            dto.stationName(),
            dto.content(),
            dto.totalRating(),
            dto.tasteRating(),
            dto.amountRating(),
            dto.priceRating(),
            dto.atmosphereRating(),
            dto.kindnessRating(),
            dto.hygieneRating(),
            dto.willRevisit(),
            dto.memberId(),
            dto.memberNickname(),
            dto.memberProfileImageUrl(),
            dto.createdAt(),
            dto.imageUrls(),
            dto.tagNames(),
            dto.ownerOnly(),
            dto.ownerReplyContent(),
            dto.ownerReplyCreatedAt(),
            orderMethod,
            author ? dto.deliveryRating() : null,
            author ? dto.deliveryComment() : null
        );
    }
}
