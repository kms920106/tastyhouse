package com.tastyhouse.application.review.store;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestStatePort;

public class ReviewBlindRequestStore implements ReviewBlindRequestRepository {
    private static final List<String> TERMINATED_STATUSES = List.of(
        ReviewBlindStatus.APPROVED.name(),
        ReviewBlindStatus.REJECTED.name(),
        ReviewBlindStatus.EXPIRED.name(),
        ReviewBlindStatus.DELETED.name()
    );

    private final ReviewBlindRequestStatePort reviewBlindRequestStatePort;

    public ReviewBlindRequestStore(ReviewBlindRequestStatePort reviewBlindRequestStatePort) {
        this.reviewBlindRequestStatePort = reviewBlindRequestStatePort;
    }

    @Override
    public Optional<ReviewBlindRequest> findById(ReviewBlindRequestId reviewBlindRequestId) {
        return reviewBlindRequestStatePort.findById(reviewBlindRequestId.value())
            .map(ReviewBlindRequestStateMapper::toDomain);
    }

    @Override
    public boolean existsByReviewIdAndStatus(ReviewId reviewId, ReviewBlindStatus status) {
        return reviewBlindRequestStatePort.existsByReviewIdAndStatus(reviewId.value(), status.name());
    }

    @Override
    public boolean existsTerminatedByReviewId(ReviewId reviewId) {
        return reviewBlindRequestStatePort.existsByReviewIdAndStatusIn(reviewId.value(), TERMINATED_STATUSES);
    }

    @Override
    public List<ReviewBlindRequest> findExpirableBlinds(LocalDateTime now) {
        return reviewBlindRequestStatePort.findByStatusExpiringBefore(ReviewBlindStatus.APPROVED.name(), now).stream()
            .map(ReviewBlindRequestStateMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ReviewBlindRequest> findApprovedByReviewId(ReviewId reviewId) {
        return reviewBlindRequestStatePort.findLatestByReviewIdAndStatus(reviewId.value(), ReviewBlindStatus.APPROVED.name())
            .map(ReviewBlindRequestStateMapper::toDomain);
    }

    @Override
    public ReviewBlindRequest save(ReviewBlindRequest reviewBlindRequest) {
        return ReviewBlindRequestStateMapper.toDomain(
            reviewBlindRequestStatePort.save(ReviewBlindRequestStateMapper.toState(reviewBlindRequest)));
    }
}
