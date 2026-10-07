package com.tastyhouse.application.review.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.product.port.out.ProductQueryPort;
import com.tastyhouse.application.review.port.in.ReviewProductQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewDetailResult;
import com.tastyhouse.application.review.port.out.ReviewProductView;

@Service
@Transactional(readOnly = true)
class ReviewProductQueryService implements ReviewProductQueryUseCase {

    private final ReviewDetailReader reviewDetailReader;
    private final ProductQueryPort productQueryPort;

    public ReviewProductQueryService(
        ReviewDetailReader reviewDetailReader,
        ProductQueryPort productQueryPort
    ) {
        this.reviewDetailReader = reviewDetailReader;
        this.productQueryPort = productQueryPort;
    }

    @Override
    public Optional<ReviewProductView> findReviewProduct(Long reviewId, Long viewerMemberId) {
        Optional<ReviewDetailResult> reviewDetailOpt = reviewDetailReader.findReviewDetailResult(ReviewId.of(reviewId), viewerMemberId);
        if (reviewDetailOpt.isEmpty()) {
            return Optional.empty();
        }

        ReviewDetailResult reviewDetail = reviewDetailOpt.get();

        List<String> reviewImageUrls = reviewDetail.imageUrls();
        String reviewMemberProfileImageUrl = reviewDetail.memberProfileImageUrl();

        return productQueryPort.findProductDetailById(reviewDetailReader.findProductIdOfReview(reviewId))
            .map(product -> {
                Integer price = product.discountPrice() != null
                    ? product.discountPrice()
                    : product.originalPrice();

                return new ReviewProductView(
                    product.id(),
                    product.name(),
                    getFirstImageUrl(product.id()),
                    price,
                    reviewDetail.id(),
                    reviewDetail.content(),
                    reviewDetail.totalRating(),
                    reviewDetail.tasteRating(),
                    reviewDetail.amountRating(),
                    reviewDetail.priceRating(),
                    reviewDetail.atmosphereRating(),
                    reviewDetail.kindnessRating(),
                    reviewDetail.hygieneRating(),
                    reviewDetail.willRevisit(),
                    reviewDetail.memberId(),
                    reviewDetail.memberNickname(),
                    reviewMemberProfileImageUrl,
                    reviewDetail.createdAt(),
                    reviewImageUrls,
                    reviewDetail.tagNames()
                );
            })
            .or(() -> Optional.of(
                new ReviewProductView(
                    null, null, null, null,
                    reviewDetail.id(),
                    reviewDetail.content(),
                    reviewDetail.totalRating(),
                    reviewDetail.tasteRating(),
                    reviewDetail.amountRating(),
                    reviewDetail.priceRating(),
                    reviewDetail.atmosphereRating(),
                    reviewDetail.kindnessRating(),
                    reviewDetail.hygieneRating(),
                    reviewDetail.willRevisit(),
                    reviewDetail.memberId(),
                    reviewDetail.memberNickname(),
                    reviewMemberProfileImageUrl,
                    reviewDetail.createdAt(),
                    reviewImageUrls,
                    reviewDetail.tagNames()
                )
            ));
    }

    private String getFirstImageUrl(Long productId) {
        return productQueryPort.findProductImageUrls(productId).stream()
            .findFirst()
            .orElse(null);
    }
}
