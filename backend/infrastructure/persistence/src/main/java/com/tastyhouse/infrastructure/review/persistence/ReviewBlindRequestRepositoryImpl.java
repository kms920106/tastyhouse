package com.tastyhouse.infrastructure.review.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.review.repository.ReviewBlindRequestRepository;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.domain.review.vo.ReviewId;

import static com.tastyhouse.infrastructure.review.persistence.QReviewBlindRequestJpaEntity.reviewBlindRequestJpaEntity;

@Repository
public class ReviewBlindRequestRepositoryImpl implements ReviewBlindRequestRepository {
    private static final List<ReviewBlindStatus> TERMINATED_STATUSES = List.of(
        ReviewBlindStatus.APPROVED,
        ReviewBlindStatus.REJECTED,
        ReviewBlindStatus.EXPIRED,
        ReviewBlindStatus.DELETED
    );

    private final JPAQueryFactory queryFactory;
    private final ReviewBlindRequestJpaRepository reviewBlindRequestJpaRepository;

    public ReviewBlindRequestRepositoryImpl(JPAQueryFactory queryFactory, ReviewBlindRequestJpaRepository reviewBlindRequestJpaRepository) {
        this.queryFactory = queryFactory;
        this.reviewBlindRequestJpaRepository = reviewBlindRequestJpaRepository;
    }

    @Override
    public Optional<ReviewBlindRequest> findById(ReviewBlindRequestId reviewBlindRequestId) {
        return reviewBlindRequestJpaRepository.findById(reviewBlindRequestId.value())
            .map(ReviewBlindRequestMapper::toDomain);
    }

    @Override
    public boolean existsByReviewIdAndStatus(ReviewId reviewId, ReviewBlindStatus status) {
        Integer result = queryFactory
            .selectOne()
            .from(reviewBlindRequestJpaEntity)
            .where(
                reviewBlindRequestJpaEntity.reviewId.eq(reviewId.value()),
                reviewBlindRequestJpaEntity.status.eq(status)
            )
            .fetchFirst();
        return result != null;
    }

    @Override
    public boolean existsTerminatedByReviewId(ReviewId reviewId) {
        Integer result = queryFactory
            .selectOne()
            .from(reviewBlindRequestJpaEntity)
            .where(
                reviewBlindRequestJpaEntity.reviewId.eq(reviewId.value()),
                reviewBlindRequestJpaEntity.status.in(TERMINATED_STATUSES)
            )
            .fetchFirst();
        return result != null;
    }

    @Override
    public List<ReviewBlindRequest> findExpirableBlinds(LocalDateTime now) {
        return queryFactory
            .selectFrom(reviewBlindRequestJpaEntity)
            .where(
                reviewBlindRequestJpaEntity.status.eq(ReviewBlindStatus.APPROVED),
                reviewBlindRequestJpaEntity.blindUntil.isNotNull(),
                reviewBlindRequestJpaEntity.blindUntil.loe(now)
            )
            .orderBy(reviewBlindRequestJpaEntity.blindUntil.asc())
            .fetch()
            .stream()
            .map(ReviewBlindRequestMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ReviewBlindRequest> findApprovedByReviewId(ReviewId reviewId) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(reviewBlindRequestJpaEntity)
                .where(
                    reviewBlindRequestJpaEntity.reviewId.eq(reviewId.value()),
                    reviewBlindRequestJpaEntity.status.eq(ReviewBlindStatus.APPROVED)
                )
                .orderBy(reviewBlindRequestJpaEntity.id.desc())
                .fetchFirst()
        ).map(ReviewBlindRequestMapper::toDomain);
    }

    @Override
    public ReviewBlindRequest save(ReviewBlindRequest reviewBlindRequest) {
        if (reviewBlindRequest.getId() == null) {
            ReviewBlindRequestJpaEntity saved =
                reviewBlindRequestJpaRepository.save(ReviewBlindRequestMapper.toEntity(reviewBlindRequest));
            return ReviewBlindRequestMapper.toDomain(saved);
        }

        ReviewBlindRequestJpaEntity entity = reviewBlindRequestJpaRepository.findById(reviewBlindRequest.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 리뷰 게시중단 요청입니다: " + reviewBlindRequest.getId()));
        ReviewBlindRequestMapper.applyChanges(entity, reviewBlindRequest);
        return ReviewBlindRequestMapper.toDomain(entity);
    }
}
