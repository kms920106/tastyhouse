package com.tastyhouse.infrastructure.review.persistence;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.review.domain.model.ReviewComment;
import com.tastyhouse.core.domain.review.domain.repository.ReviewCommentRepository;
import com.tastyhouse.core.domain.review.domain.vo.ReviewCommentId;

@Repository
@RequiredArgsConstructor
public class ReviewCommentRepositoryImpl implements ReviewCommentRepository {

    private final ReviewCommentJpaRepository reviewCommentJpaRepository;

    @Override
    public Optional<ReviewComment> findById(ReviewCommentId commentId) {
        return reviewCommentJpaRepository.findById(commentId.value()).map(ReviewCommentMapper::toDomain);
    }

    @Override
    public ReviewComment save(ReviewComment comment) {
        if (comment.getId() == null) {
            ReviewCommentJpaEntity saved = reviewCommentJpaRepository.save(ReviewCommentMapper.toEntity(comment));
            return ReviewCommentMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ReviewCommentJpaEntity entity = reviewCommentJpaRepository.findById(comment.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 리뷰 댓글입니다: " + comment.getId()));
        ReviewCommentMapper.applyChanges(entity, comment);
        return ReviewCommentMapper.toDomain(entity);
    }

    @Override
    public void deleteById(ReviewCommentId commentId) {
        reviewCommentJpaRepository.deleteById(commentId.value());
    }
}
