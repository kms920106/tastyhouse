package com.tastyhouse.core.domain.review.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.review.domain.model.ReviewTag;
import com.tastyhouse.core.domain.review.domain.repository.ReviewTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tastyhouse.core.domain.review.domain.model.QReviewTag.reviewTag;

@Repository
@RequiredArgsConstructor
public class ReviewTagRepositoryImpl implements ReviewTagRepository {

    private final JPAQueryFactory queryFactory;
    private final ReviewTagJpaRepository reviewTagJpaRepository;

    @Override
    public List<Long> findTagIdsByReviewId(Long reviewId) {
        return queryFactory
            .select(reviewTag.tagId)
            .from(reviewTag)
            .where(reviewTag.reviewId.eq(reviewId))
            .fetch();
    }

    @Override
    public void saveAll(List<ReviewTag> tags) {
        reviewTagJpaRepository.saveAll(tags);
    }

    @Override
    public void deleteByReviewId(Long reviewId) {
        queryFactory
            .delete(reviewTag)
            .where(reviewTag.reviewId.eq(reviewId))
            .execute();
    }
}
