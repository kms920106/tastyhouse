package com.tastyhouse.infrastructure.review.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.review.domain.model.ReviewReply;
import com.tastyhouse.core.domain.review.domain.repository.ReviewReplyRepository;
import com.tastyhouse.core.domain.review.domain.vo.ReviewCommentId;
import com.tastyhouse.core.domain.review.domain.vo.ReviewReplyId;

import static com.tastyhouse.infrastructure.review.persistence.QReviewReplyJpaEntity.reviewReplyJpaEntity;

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
            .selectFrom(reviewReplyJpaEntity)
            .where(
                reviewReplyJpaEntity.commentId.in(commentIdValues),
                reviewReplyJpaEntity.hidden.eq(false)
            )
            .orderBy(reviewReplyJpaEntity.createdAt.asc())
            .fetch()
            .stream()
            .map(ReviewReplyMapper::toDomain)
            .toList();
    }

    @Override
    public List<ReviewReply> findByCommentIdInOrderByCreatedAtAsc(List<ReviewCommentId> commentIds) {
        List<Long> commentIdValues = commentIds.stream()
            .map(ReviewCommentId::value)
            .toList();
        return queryFactory
            .selectFrom(reviewReplyJpaEntity)
            .where(reviewReplyJpaEntity.commentId.in(commentIdValues))
            .orderBy(reviewReplyJpaEntity.createdAt.asc())
            .fetch()
            .stream()
            .map(ReviewReplyMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ReviewReply> findById(ReviewReplyId replyId) {
        return reviewReplyJpaRepository.findById(replyId.value()).map(ReviewReplyMapper::toDomain);
    }

    @Override
    public ReviewReply save(ReviewReply reply) {
        if (reply.getId() == null) {
            ReviewReplyJpaEntity saved = reviewReplyJpaRepository.save(ReviewReplyMapper.toEntity(reply));
            return ReviewReplyMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ReviewReplyJpaEntity entity = reviewReplyJpaRepository.findById(reply.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 리뷰 답글입니다: " + reply.getId()));
        ReviewReplyMapper.applyChanges(entity, reply);
        return ReviewReplyMapper.toDomain(entity);
    }

    @Override
    public void deleteById(ReviewReplyId replyId) {
        reviewReplyJpaRepository.deleteById(replyId.value());
    }
}
