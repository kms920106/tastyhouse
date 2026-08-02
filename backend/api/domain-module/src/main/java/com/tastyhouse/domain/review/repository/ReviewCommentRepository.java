package com.tastyhouse.domain.review.repository;

import java.util.Optional;

import com.tastyhouse.domain.review.model.ReviewComment;
import com.tastyhouse.domain.review.vo.ReviewCommentId;

/**
 * 리뷰 댓글 write 포트.
 *
 * <p>목록 조회는 표현 목적 read이므로 infrastructure-module의 query DAO로 이관했고(공통 지침 패턴 4),
 * 여기에는 상태 전이·삭제에 필요한 단건 로드와 저장만 남긴다.
 */
public interface ReviewCommentRepository {

    Optional<ReviewComment> findById(ReviewCommentId commentId);

    ReviewComment save(ReviewComment comment);

    void deleteById(ReviewCommentId commentId);
}
