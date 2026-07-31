package com.tastyhouse.domain.review.domain.repository;

import java.util.Optional;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.review.domain.model.Review;
import com.tastyhouse.domain.review.domain.vo.ReviewId;

/**
 * 리뷰 write 포트.
 *
 * <p>도메인 모델을 주고받는 CRUD만 남긴다(공통 지침 패턴 4). 목록·검색·통계 등 표현 목적 read는
 * infrastructure-module의 {@code ReviewQueryDao}/{@code ReviewManagementQueryDao}가 담당한다.
 *
 * <p>{@code findByIdAndMemberId}는 조회처럼 보이지만 "본인 리뷰만 수정·삭제할 수 있다"는 소유권
 * 불변식을 검증하는 command 경로 전용 로드이므로 여기 남는다.
 */
public interface ReviewRepository {

    Optional<Review> findById(ReviewId reviewId);

    Optional<Review> findByIdAndMemberId(ReviewId reviewId, MemberId memberId);

    Review save(Review review);

    void deleteById(ReviewId reviewId);
}
