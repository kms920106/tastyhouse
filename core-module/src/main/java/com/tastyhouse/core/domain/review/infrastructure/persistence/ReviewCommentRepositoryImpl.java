package com.tastyhouse.core.domain.review.infrastructure.persistence;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.review.domain.model.ReviewComment;
import com.tastyhouse.core.domain.review.domain.repository.ReviewCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tastyhouse.core.domain.review.domain.model.QReviewComment.reviewComment;

@Repository
@RequiredArgsConstructor
public class ReviewCommentRepositoryImpl implements ReviewCommentRepository {

    private final JPAQueryFactory queryFactory;
    private final ReviewCommentJpaRepository reviewCommentJpaRepository;

    @Override
    public List<ReviewComment> findByReviewIdOrderByCreatedAtDesc(Long reviewId) {
        return queryFactory
            .selectFrom(reviewComment)
            .where(
                reviewComment.reviewId.eq(reviewId),
                reviewComment.hidden.eq(false)
            )
            .orderBy(reviewComment.createdAt.desc())
            .fetch();
    }

    @Override
    public Map<Long, Long> countByReviewIdIn(List<Long> reviewIds) {
        List<Tuple> results = queryFactory
            .select(reviewComment.reviewId, reviewComment.count())
            .from(reviewComment)
            .where(
                reviewComment.reviewId.in(reviewIds),
                reviewComment.hidden.eq(false)
            )
            .groupBy(reviewComment.reviewId)
            .fetch();

        Map<Long, Long> countMap = new HashMap<>();
        for (Tuple row : results) {
            countMap.put(row.get(0, Long.class), row.get(1, Long.class));
        }
        return countMap;
    }

    @Override
    public ReviewComment save(ReviewComment comment) {
        return reviewCommentJpaRepository.save(comment);
    }
}
