package com.tastyhouse.domain.review.repository;

import java.util.Optional;

import com.tastyhouse.domain.review.model.ReviewOwnerReply;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.review.vo.ReviewOwnerReplyId;

/**
 * 사장님 답변 write 포트.
 *
 * <p>{@code findByReviewId}·{@code existsByReviewId}는 조회처럼 보이지만 "리뷰당 답변 1건"이라는 불변식을
 * 검증하고 수정·삭제 대상을 로드하는 command 경로 전용이므로 여기 남는다(write 포트 잔류 판정 기준).
 * 목록·상세 화면용 답변 표시는 {@code ShopReviewManagementQueryDao}가 join으로 투영한다.
 */
public interface ReviewOwnerReplyRepository {

    Optional<ReviewOwnerReply> findById(ReviewOwnerReplyId reviewOwnerReplyId);

    Optional<ReviewOwnerReply> findByReviewId(ReviewId reviewId);

    boolean existsByReviewId(ReviewId reviewId);

    ReviewOwnerReply save(ReviewOwnerReply reviewOwnerReply);

    void delete(ReviewOwnerReply reviewOwnerReply);
}
