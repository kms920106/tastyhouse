package com.tastyhouse.infrastructure.review.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.review.port.out.write.ReviewState;
import com.tastyhouse.application.review.port.out.write.ReviewStatePort;

import static com.tastyhouse.infrastructure.review.persistence.QReviewJpaEntity.reviewJpaEntity;

@Repository
public class ReviewStatePortImpl implements ReviewStatePort {
    private final JPAQueryFactory queryFactory;
    private final ReviewJpaRepository reviewJpaRepository;

    public ReviewStatePortImpl(JPAQueryFactory queryFactory, ReviewJpaRepository reviewJpaRepository) {
        this.queryFactory = queryFactory;
        this.reviewJpaRepository = reviewJpaRepository;
    }

    @Override
    public Optional<ReviewState> findById(Long id) {
        return reviewJpaRepository.findById(id).map(ReviewMapper::toState);
    }

    @Override
    public Optional<ReviewState> findByIdAndMemberId(Long id, Long memberId) {
        ReviewJpaEntity result = queryFactory
            .selectFrom(reviewJpaEntity)
            .where(
                reviewJpaEntity.id.eq(id),
                reviewJpaEntity.memberId.eq(memberId)
            )
            .fetchOne();
        return Optional.ofNullable(result).map(ReviewMapper::toState);
    }

    @Override
    public boolean existsByOrderIdAndProductId(Long orderId, Long productId) {
        Integer result = queryFactory
            .selectOne()
            .from(reviewJpaEntity)
            .where(
                reviewJpaEntity.orderId.eq(orderId),
                reviewJpaEntity.productId.eq(productId)
            )
            .fetchFirst();
        return result != null;
    }

    @Override
    public ReviewState save(ReviewState state) {
        if (state.id() == null) {
            ReviewJpaEntity saved = reviewJpaRepository.save(ReviewMapper.toEntity(state));
            return ReviewMapper.toState(saved);
        }

        ReviewJpaEntity entity = reviewJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 리뷰입니다: " + state.id()));
        ReviewMapper.applyChanges(entity, state);
        return ReviewMapper.toState(entity);
    }

    @Override
    public void deleteById(Long id) {
        reviewJpaRepository.deleteById(id);
    }
}
