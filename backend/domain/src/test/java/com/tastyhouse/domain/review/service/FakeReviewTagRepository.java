package com.tastyhouse.domain.review.service;

import java.util.List;

import com.tastyhouse.domain.review.model.ReviewTag;
import com.tastyhouse.domain.review.repository.ReviewTagRepository;
import com.tastyhouse.domain.review.vo.ReviewId;

/**
 * {@link ReviewTagRepository}에 조회 메서드가 없어, 저장한 태그를 되읽어 검증할 수단이 없다.
 * 따라서 보관용 컬렉션을 두지 않고 호출을 삼키기만 한다 — 협력 객체를 채우는 용도의 스텁이다.
 */
public class FakeReviewTagRepository implements ReviewTagRepository {
    @Override
    public void saveAll(List<ReviewTag> tags) {
        // 조회 계약이 없어 보관하지 않는다
    }

    @Override
    public void deleteByReviewId(ReviewId reviewId) {
        // 조회 계약이 없어 보관하지 않는다
    }
}
