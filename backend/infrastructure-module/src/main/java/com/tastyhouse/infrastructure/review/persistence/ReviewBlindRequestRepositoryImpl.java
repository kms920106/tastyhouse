package com.tastyhouse.infrastructure.review.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.repository.ReviewBlindRequestRepository;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

import static com.tastyhouse.infrastructure.review.persistence.QReviewBlindRequestJpaEntity.reviewBlindRequestJpaEntity;

@Repository
public class ReviewBlindRequestRepositoryImpl implements ReviewBlindRequestRepository {

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
    public boolean existsByReviewIdAndStatus(ReviewId reviewId, ApprovalStatus status) {
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
    public ReviewBlindRequest save(ReviewBlindRequest reviewBlindRequest) {
        if (reviewBlindRequest.getId() == null) {
            ReviewBlindRequestJpaEntity saved =
                reviewBlindRequestJpaRepository.save(ReviewBlindRequestMapper.toEntity(reviewBlindRequest));
            return ReviewBlindRequestMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ReviewBlindRequestJpaEntity entity = reviewBlindRequestJpaRepository.findById(reviewBlindRequest.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 리뷰 게시중단 요청입니다: " + reviewBlindRequest.getId()));
        ReviewBlindRequestMapper.applyChanges(entity, reviewBlindRequest);
        return ReviewBlindRequestMapper.toDomain(entity);
    }
}
