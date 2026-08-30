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

    /**
     * 1회 제한의 판정 대상 — <b>{@code CANCELED}는 제외</b>한다(점주가 스스로 철회한 건은 심사 자원을
     * 쓰지 않았으므로 1회 소진이 아니다). 상세는 write 포트 Javadoc 참고.
     */
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

    /**
     * {@code status = APPROVED AND blind_until <= now}. {@code idx_review_blind_request_blind_until}이
     * 이 조회의 인덱스다.
     */
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

    /**
     * 리뷰에 걸린 현재 게시중단 요청.
     *
     * <p>1회 제한 덕분에 {@code APPROVED}는 리뷰당 최대 1건이지만, 그 제한이 애플리케이션 검사라
     * 동시 요청에는 이론상 뚫린다. 따라서 {@code fetchOne}(2건이면 예외) 대신 최신 1건을 취해
     * 조회가 실패하지 않게 한다.
     */
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

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ReviewBlindRequestJpaEntity entity = reviewBlindRequestJpaRepository.findById(reviewBlindRequest.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 리뷰 게시중단 요청입니다: " + reviewBlindRequest.getId()));
        ReviewBlindRequestMapper.applyChanges(entity, reviewBlindRequest);
        return ReviewBlindRequestMapper.toDomain(entity);
    }
}
