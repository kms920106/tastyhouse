package com.tastyhouse.infrastructure.review.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.review.port.out.write.ReviewCommentState;
import com.tastyhouse.application.review.port.out.write.ReviewCommentStatePort;

@Repository
public class ReviewCommentStatePortImpl implements ReviewCommentStatePort {
    private final ReviewCommentJpaRepository reviewCommentJpaRepository;

    public ReviewCommentStatePortImpl(ReviewCommentJpaRepository reviewCommentJpaRepository) {
        this.reviewCommentJpaRepository = reviewCommentJpaRepository;
    }

    @Override
    public Optional<ReviewCommentState> findById(Long id) {
        return reviewCommentJpaRepository.findById(id).map(ReviewCommentMapper::toState);
    }

    @Override
    public ReviewCommentState save(ReviewCommentState state) {
        if (state.id() == null) {
            ReviewCommentJpaEntity saved = reviewCommentJpaRepository.save(ReviewCommentMapper.toEntity(state));
            return ReviewCommentMapper.toState(saved);
        }

        ReviewCommentJpaEntity entity = reviewCommentJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 리뷰 댓글입니다: " + state.id()));
        ReviewCommentMapper.applyChanges(entity, state);
        return ReviewCommentMapper.toState(entity);
    }

    @Override
    public void deleteById(Long id) {
        reviewCommentJpaRepository.deleteById(id);
    }
}
