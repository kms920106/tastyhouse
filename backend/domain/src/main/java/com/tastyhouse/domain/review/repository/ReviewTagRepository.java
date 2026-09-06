package com.tastyhouse.domain.review.repository;

import java.util.List;

import com.tastyhouse.domain.review.model.ReviewTag;
import com.tastyhouse.domain.review.vo.ReviewId;

/**
 * 리뷰 태그 write 포트.
 *
 * <p>태그명 조회는 표현 목적 read이므로 infrastructure-module의 query DAO로 이관했고(공통 지침 패턴 4),
 * 여기에는 리뷰 등록·수정 시의 일괄 적재와 교체용 삭제만 남긴다.
 */
public interface ReviewTagRepository {

    void saveAll(List<ReviewTag> tags);

    void deleteByReviewId(ReviewId reviewId);
}
