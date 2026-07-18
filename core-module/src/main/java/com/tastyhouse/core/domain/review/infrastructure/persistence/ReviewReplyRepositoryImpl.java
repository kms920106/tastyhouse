package com.tastyhouse.core.domain.review.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.review.domain.model.ReviewReply;
import com.tastyhouse.core.domain.review.domain.repository.ReviewReplyRepository;
import com.tastyhouse.core.domain.review.domain.vo.ReviewCommentId;
import com.tastyhouse.core.domain.review.domain.vo.ReviewReplyId;

import static com.tastyhouse.core.domain.review.domain.model.QReviewReply.reviewReply;

@Repository
@RequiredArgsConstructor
public class ReviewReplyRepositoryImpl implements ReviewReplyRepository {

    private final JPAQueryFactory queryFactory;
    private final ReviewReplyJpaRepository reviewReplyJpaRepository;

    @Override
    public List<ReviewReply> findByCommentIdInAndHiddenFalseOrderByCreatedAtAsc(List<ReviewCommentId> commentIds) {
        List<Long> commentIdValues = commentIds.stream()
            .map(ReviewCommentId::value)
            .toList();
        return queryFactory
            .selectFrom(reviewReply)
            .where(
                reviewReply.commentId.in(commentIdValues),
                reviewReply.hidden.eq(false)
            )
            .orderBy(reviewReply.createdAt.asc())
            .fetch();
    }

    @Override
    public List<ReviewReply> findByCommentIdInOrderByCreatedAtAsc(List<ReviewCommentId> commentIds) {
        List<Long> commentIdValues = commentIds.stream()
            .map(ReviewCommentId::value)
            .toList();
        return queryFactory
            .selectFrom(reviewReply)
            .where(reviewReply.commentId.in(commentIdValues))
            .orderBy(reviewReply.createdAt.asc())
            .fetch();
    }

    @Override
    public Optional<ReviewReply> findById(ReviewReplyId replyId) {
        return reviewReplyJpaRepository.findById(replyId.value());
    }

    @Override
    public ReviewReply save(ReviewReply reply) {
        return reviewReplyJpaRepository.save(reply);
    }

    @Override
    public void deleteById(ReviewReplyId replyId) {
        reviewReplyJpaRepository.deleteById(replyId.value());
    }
}
