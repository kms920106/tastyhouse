package com.tastyhouse.core.repository.review;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.review.ReviewReply;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tastyhouse.core.entity.review.QReviewReply.reviewReply;

@Repository
@RequiredArgsConstructor
public class ReviewReplyRepositoryImpl implements ReviewReplyRepository {

    private final JPAQueryFactory queryFactory;
    private final ReviewReplyJpaRepository reviewReplyJpaRepository;

    @Override
    public List<ReviewReply> findByCommentIdAndIsHiddenFalseOrderByCreatedAtAsc(Long commentId) {
        return queryFactory
            .selectFrom(reviewReply)
            .where(
                reviewReply.commentId.eq(commentId),
                reviewReply.isHidden.eq(false)
            )
            .orderBy(reviewReply.createdAt.asc())
            .fetch();
    }

    @Override
    public List<ReviewReply> findByCommentIdInAndIsHiddenFalseOrderByCreatedAtAsc(List<Long> commentIds) {
        return queryFactory
            .selectFrom(reviewReply)
            .where(
                reviewReply.commentId.in(commentIds),
                reviewReply.isHidden.eq(false)
            )
            .orderBy(reviewReply.createdAt.asc())
            .fetch();
    }

    @Override
    public ReviewReply save(ReviewReply reply) {
        return reviewReplyJpaRepository.save(reply);
    }
}
