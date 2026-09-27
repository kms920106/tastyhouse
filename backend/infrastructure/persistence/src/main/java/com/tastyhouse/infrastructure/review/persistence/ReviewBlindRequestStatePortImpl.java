package com.tastyhouse.infrastructure.review.persistence;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestState;
import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestStatePort;

import static com.tastyhouse.infrastructure.review.persistence.QReviewBlindRequestJpaEntity.reviewBlindRequestJpaEntity;

@Repository
public class ReviewBlindRequestStatePortImpl implements ReviewBlindRequestStatePort {
    private final JPAQueryFactory queryFactory;
    private final ReviewBlindRequestJpaRepository reviewBlindRequestJpaRepository;

    public ReviewBlindRequestStatePortImpl(JPAQueryFactory queryFactory, ReviewBlindRequestJpaRepository reviewBlindRequestJpaRepository) {
        this.queryFactory = queryFactory;
        this.reviewBlindRequestJpaRepository = reviewBlindRequestJpaRepository;
    }

    @Override
    public Optional<ReviewBlindRequestState> findById(Long id) {
        return reviewBlindRequestJpaRepository.findById(id)
            .map(ReviewBlindRequestMapper::toState);
    }

    @Override
    public boolean existsByReviewIdAndStatus(Long reviewId, String status) {
        Integer result = queryFactory
            .selectOne()
            .from(reviewBlindRequestJpaEntity)
            .where(
                reviewBlindRequestJpaEntity.reviewId.eq(reviewId),
                reviewBlindRequestJpaEntity.status.eq(status)
            )
            .fetchFirst();
        return result != null;
    }

    @Override
    public boolean existsByReviewIdAndStatusIn(Long reviewId, Collection<String> statuses) {
        Integer result = queryFactory
            .selectOne()
            .from(reviewBlindRequestJpaEntity)
            .where(
                reviewBlindRequestJpaEntity.reviewId.eq(reviewId),
                reviewBlindRequestJpaEntity.status.in(statuses)
            )
            .fetchFirst();
        return result != null;
    }

    @Override
    public List<ReviewBlindRequestState> findByStatusExpiringBefore(String status, LocalDateTime now) {
        return queryFactory
            .selectFrom(reviewBlindRequestJpaEntity)
            .where(
                reviewBlindRequestJpaEntity.status.eq(status),
                reviewBlindRequestJpaEntity.blindUntil.isNotNull(),
                reviewBlindRequestJpaEntity.blindUntil.loe(now)
            )
            .orderBy(reviewBlindRequestJpaEntity.blindUntil.asc())
            .fetch()
            .stream()
            .map(ReviewBlindRequestMapper::toState)
            .toList();
    }

    @Override
    public Optional<ReviewBlindRequestState> findLatestByReviewIdAndStatus(Long reviewId, String status) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(reviewBlindRequestJpaEntity)
                .where(
                    reviewBlindRequestJpaEntity.reviewId.eq(reviewId),
                    reviewBlindRequestJpaEntity.status.eq(status)
                )
                .orderBy(reviewBlindRequestJpaEntity.id.desc())
                .fetchFirst()
        ).map(ReviewBlindRequestMapper::toState);
    }

    @Override
    public ReviewBlindRequestState save(ReviewBlindRequestState state) {
        if (state.id() == null) {
            ReviewBlindRequestJpaEntity saved =
                reviewBlindRequestJpaRepository.save(ReviewBlindRequestMapper.toEntity(state));
            return ReviewBlindRequestMapper.toState(saved);
        }

        ReviewBlindRequestJpaEntity entity = reviewBlindRequestJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 리뷰 게시중단 요청입니다: " + state.id()));
        ReviewBlindRequestMapper.applyChanges(entity, state);
        return ReviewBlindRequestMapper.toState(entity);
    }
}
