package com.tastyhouse.infrastructure.review.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.review.port.out.write.ReviewOwnerReplyState;
import com.tastyhouse.application.review.port.out.write.ReviewOwnerReplyStatePort;

import static com.tastyhouse.infrastructure.review.persistence.QReviewOwnerReplyJpaEntity.reviewOwnerReplyJpaEntity;

@Repository
public class ReviewOwnerReplyStatePortImpl implements ReviewOwnerReplyStatePort {
    private final JPAQueryFactory queryFactory;
    private final ReviewOwnerReplyJpaRepository reviewOwnerReplyJpaRepository;

    public ReviewOwnerReplyStatePortImpl(JPAQueryFactory queryFactory, ReviewOwnerReplyJpaRepository reviewOwnerReplyJpaRepository) {
        this.queryFactory = queryFactory;
        this.reviewOwnerReplyJpaRepository = reviewOwnerReplyJpaRepository;
    }

    @Override
    public Optional<ReviewOwnerReplyState> findById(Long id) {
        return reviewOwnerReplyJpaRepository.findById(id)
            .map(ReviewOwnerReplyMapper::toState);
    }

    @Override
    public Optional<ReviewOwnerReplyState> findByReviewId(Long reviewId) {
        ReviewOwnerReplyJpaEntity entity = queryFactory
            .selectFrom(reviewOwnerReplyJpaEntity)
            .where(reviewOwnerReplyJpaEntity.reviewId.eq(reviewId))
            .fetchOne();

        return Optional.ofNullable(entity).map(ReviewOwnerReplyMapper::toState);
    }

    @Override
    public boolean existsByReviewId(Long reviewId) {
        Integer result = queryFactory
            .selectOne()
            .from(reviewOwnerReplyJpaEntity)
            .where(reviewOwnerReplyJpaEntity.reviewId.eq(reviewId))
            .fetchFirst();
        return result != null;
    }

    @Override
    public ReviewOwnerReplyState save(ReviewOwnerReplyState state) {
        if (state.id() == null) {
            ReviewOwnerReplyJpaEntity saved =
                reviewOwnerReplyJpaRepository.save(ReviewOwnerReplyMapper.toEntity(state));
            return ReviewOwnerReplyMapper.toState(saved);
        }

        ReviewOwnerReplyJpaEntity entity = reviewOwnerReplyJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 사장님 답변입니다: " + state.id()));
        ReviewOwnerReplyMapper.applyChanges(entity, state);
        return ReviewOwnerReplyMapper.toState(entity);
    }

    @Override
    public void delete(ReviewOwnerReplyState state) {
        reviewOwnerReplyJpaRepository.deleteById(state.id());
    }
}
