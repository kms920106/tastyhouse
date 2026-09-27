package com.tastyhouse.infrastructure.review.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.review.port.out.write.ReviewReplyState;
import com.tastyhouse.application.review.port.out.write.ReviewReplyStatePort;

@Repository
public class ReviewReplyStatePortImpl implements ReviewReplyStatePort {
    private final ReviewReplyJpaRepository reviewReplyJpaRepository;

    public ReviewReplyStatePortImpl(ReviewReplyJpaRepository reviewReplyJpaRepository) {
        this.reviewReplyJpaRepository = reviewReplyJpaRepository;
    }

    @Override
    public Optional<ReviewReplyState> findById(Long id) {
        return reviewReplyJpaRepository.findById(id).map(ReviewReplyMapper::toState);
    }

    @Override
    public ReviewReplyState save(ReviewReplyState state) {
        if (state.id() == null) {
            ReviewReplyJpaEntity saved = reviewReplyJpaRepository.save(ReviewReplyMapper.toEntity(state));
            return ReviewReplyMapper.toState(saved);
        }

        ReviewReplyJpaEntity entity = reviewReplyJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 리뷰 답글입니다: " + state.id()));
        ReviewReplyMapper.applyChanges(entity, state);
        return ReviewReplyMapper.toState(entity);
    }

    @Override
    public void deleteById(Long id) {
        reviewReplyJpaRepository.deleteById(id);
    }
}
