package com.tastyhouse.core.domain.review.infrastructure.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.review.domain.model.ReviewComment;
import com.tastyhouse.core.domain.review.domain.repository.ReviewCommentRepository;
import com.tastyhouse.core.domain.review.domain.vo.ReviewId;

import static com.tastyhouse.core.domain.review.domain.model.QReviewComment.reviewComment;

@Repository
@RequiredArgsConstructor
public class ReviewCommentRepositoryImpl implements ReviewCommentRepository {

    private final JPAQueryFactory queryFactory;
    private final ReviewCommentJpaRepository reviewCommentJpaRepository;

    @Override
    public List<ReviewComment> findByReviewIdOrderByCreatedAtDesc(ReviewId reviewId) {
        return queryFactory
            .selectFrom(reviewComment)
            .where(
                reviewComment.reviewId.eq(reviewId.value()),
                reviewComment.hidden.eq(false)
            )
            .orderBy(reviewComment.createdAt.desc())
            .fetch();
    }

    @Override
    public ReviewComment save(ReviewComment comment) {
        return reviewCommentJpaRepository.save(comment);
    }
}
