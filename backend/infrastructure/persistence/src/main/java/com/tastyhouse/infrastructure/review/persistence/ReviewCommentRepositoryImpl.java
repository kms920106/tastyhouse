package com.tastyhouse.infrastructure.review.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.review.model.ReviewComment;
import com.tastyhouse.domain.review.repository.ReviewCommentRepository;
import com.tastyhouse.domain.review.vo.ReviewCommentId;

@Repository
public class ReviewCommentRepositoryImpl implements ReviewCommentRepository {
    private final ReviewCommentJpaRepository reviewCommentJpaRepository;

    public ReviewCommentRepositoryImpl(ReviewCommentJpaRepository reviewCommentJpaRepository) {
        this.reviewCommentJpaRepository = reviewCommentJpaRepository;
    }

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
