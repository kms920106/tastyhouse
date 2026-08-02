package com.tastyhouse.infrastructure.review.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.review.domain.model.Review;
import com.tastyhouse.domain.review.domain.repository.ReviewRepository;
import com.tastyhouse.domain.review.domain.vo.ReviewId;

import static com.tastyhouse.infrastructure.review.persistence.QReviewJpaEntity.reviewJpaEntity;

@Repository
public class ReviewRepositoryImpl implements ReviewRepository {

    private final JPAQueryFactory queryFactory;
    private final ReviewJpaRepository reviewJpaRepository;

    public ReviewRepositoryImpl(JPAQueryFactory queryFactory, ReviewJpaRepository reviewJpaRepository) {
        this.queryFactory = queryFactory;
        this.reviewJpaRepository = reviewJpaRepository;
    }

    @Override
    public Optional<Review> findById(ReviewId reviewId) {
        return reviewJpaRepository.findById(reviewId.value()).map(ReviewMapper::toDomain);
    }

    @Override
    public Optional<Review> findByIdAndMemberId(ReviewId reviewId, MemberId memberId) {
        ReviewJpaEntity result = queryFactory
            .selectFrom(reviewJpaEntity)
            .where(
                reviewJpaEntity.id.eq(reviewId.value()),
                reviewJpaEntity.memberId.eq(memberId)
            )
            .fetchOne();
        return Optional.ofNullable(result).map(ReviewMapper::toDomain);
    }

    @Override
    public Review save(Review review) {
        if (review.getId() == null) {
            ReviewJpaEntity saved = reviewJpaRepository.save(ReviewMapper.toEntity(review));
            return ReviewMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ReviewJpaEntity entity = reviewJpaRepository.findById(review.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 리뷰입니다: " + review.getId()));
        ReviewMapper.applyChanges(entity, review);
        return ReviewMapper.toDomain(entity);
    }

    @Override
    public void deleteById(ReviewId reviewId) {
        reviewJpaRepository.deleteById(reviewId.value());
    }
}
