package com.tastyhouse.domain.review.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.domain.review.vo.ReviewId;

/**
 * 리뷰 게시중단 요청 write 포트.
 *
 * <p>{@code existsByReviewIdAndStatus}는 "같은 리뷰에 PENDING 요청이 2건 생기지 않는다"는 불변식 검증용이라
 * write 포트에 남는다. <b>MySQL은 부분 인덱스를 지원하지 않아 이 중복을 UNIQUE로 막을 수 없다</b> —
 * 취소 후 재요청이 가능해야 하므로 {@code (review_id)} 유니크도 걸 수 없다. 따라서 이 검사가 유일한
 * 차단 수단이다.
 *
 * <p>{@link #existsTerminatedByReviewId}·{@link #findExpirableBlinds}·{@link #findApprovedByReviewId}도
 * 화면 조회가 아니라 <b>상태 전이의 입력</b>이라 query DAO가 아닌 이 포트에 둔다(잔류 판정 기준 —
 * <i>"이 조회가 없으면 불변식 검증이나 상태 전이가 불가능한가?"</i>). 반환 타입도 Result가 아니라
 * 도메인 모델·원시값이다.
 */
public interface ReviewBlindRequestRepository {

    Optional<ReviewBlindRequest> findById(ReviewBlindRequestId reviewBlindRequestId);

    boolean existsByReviewIdAndStatus(ReviewId reviewId, ReviewBlindStatus status);

    /**
     * 같은 리뷰에 <b>종결된</b> 요청이 있는지 검사한다(1회 제한).
     *
     * <p>판정 대상은 {@code APPROVED}/{@code REJECTED}/{@code EXPIRED}/{@code DELETED} 넷이다 —
     * <b>{@code CANCELED}는 종결로 치지 않는다.</b> 점주가 스스로 철회한 건은 심사 자원을 쓰지 않았으므로
     * 1회 소진이 아니며, 이는 기존 취소 규칙("취소 후에는 같은 리뷰에 재요청이 가능해진다")과 정합한다.
     */
    boolean existsTerminatedByReviewId(ReviewId reviewId);

    /**
     * 재노출 기한이 지난 게시중단 건을 조회한다({@code status = APPROVED AND blind_until <= now}).
     *
     * <p>{@code DELETED}·{@code EXPIRED}는 상태가 다르므로 자동으로 제외된다.
     */
    List<ReviewBlindRequest> findExpirableBlinds(LocalDateTime now);

    /**
     * 리뷰에 걸린 현재 게시중단 요청을 찾는다(고객 삭제 동의 경로의 입력).
     */
    Optional<ReviewBlindRequest> findApprovedByReviewId(ReviewId reviewId);

    ReviewBlindRequest save(ReviewBlindRequest reviewBlindRequest);
}
