package com.tastyhouse.domain.review.repository;

import java.util.Optional;

import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 리뷰 게시중단 요청 write 포트.
 *
 * <p>{@code existsByReviewIdAndStatus}는 "같은 리뷰에 PENDING 요청이 2건 생기지 않는다"는 불변식 검증용이라
 * write 포트에 남는다. <b>MySQL은 부분 인덱스를 지원하지 않아 이 중복을 UNIQUE로 막을 수 없다</b> —
 * 취소 후 재요청이 가능해야 하므로 {@code (review_id)} 유니크도 걸 수 없다. 따라서 이 검사가 유일한
 * 차단 수단이다.
 */
public interface ReviewBlindRequestRepository {

    Optional<ReviewBlindRequest> findById(ReviewBlindRequestId reviewBlindRequestId);

    boolean existsByReviewIdAndStatus(ReviewId reviewId, ApprovalStatus status);

    ReviewBlindRequest save(ReviewBlindRequest reviewBlindRequest);
}
