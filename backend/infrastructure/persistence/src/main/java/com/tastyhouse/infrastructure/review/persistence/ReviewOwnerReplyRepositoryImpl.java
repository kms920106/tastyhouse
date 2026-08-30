package com.tastyhouse.infrastructure.review.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.review.model.ReviewOwnerReply;
import com.tastyhouse.domain.review.repository.ReviewOwnerReplyRepository;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.review.vo.ReviewOwnerReplyId;

import static com.tastyhouse.infrastructure.review.persistence.QReviewOwnerReplyJpaEntity.reviewOwnerReplyJpaEntity;

@Repository
public class ReviewOwnerReplyRepositoryImpl implements ReviewOwnerReplyRepository {

    private final JPAQueryFactory queryFactory;
    private final ReviewOwnerReplyJpaRepository reviewOwnerReplyJpaRepository;

    public ReviewOwnerReplyRepositoryImpl(JPAQueryFactory queryFactory, ReviewOwnerReplyJpaRepository reviewOwnerReplyJpaRepository) {
        this.queryFactory = queryFactory;
        this.reviewOwnerReplyJpaRepository = reviewOwnerReplyJpaRepository;
    }

    @Override
    public Optional<ReviewOwnerReply> findById(ReviewOwnerReplyId reviewOwnerReplyId) {
        return reviewOwnerReplyJpaRepository.findById(reviewOwnerReplyId.value())
            .map(ReviewOwnerReplyMapper::toDomain);
    }

    @Override
    public Optional<ReviewOwnerReply> findByReviewId(ReviewId reviewId) {
        ReviewOwnerReplyJpaEntity entity = queryFactory
            .selectFrom(reviewOwnerReplyJpaEntity)
            .where(reviewOwnerReplyJpaEntity.reviewId.eq(reviewId.value()))
            .fetchOne();

        return Optional.ofNullable(entity).map(ReviewOwnerReplyMapper::toDomain);
    }

    @Override
    public boolean existsByReviewId(ReviewId reviewId) {
        Integer result = queryFactory
            .selectOne()
            .from(reviewOwnerReplyJpaEntity)
            .where(reviewOwnerReplyJpaEntity.reviewId.eq(reviewId.value()))
            .fetchFirst();
        return result != null;
    }

    @Override
    public ReviewOwnerReply save(ReviewOwnerReply reviewOwnerReply) {
        if (reviewOwnerReply.getId() == null) {
            ReviewOwnerReplyJpaEntity saved =
                reviewOwnerReplyJpaRepository.save(ReviewOwnerReplyMapper.toEntity(reviewOwnerReply));
            return ReviewOwnerReplyMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ReviewOwnerReplyJpaEntity entity = reviewOwnerReplyJpaRepository.findById(reviewOwnerReply.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 사장님 답변입니다: " + reviewOwnerReply.getId()));
        ReviewOwnerReplyMapper.applyChanges(entity, reviewOwnerReply);
        return ReviewOwnerReplyMapper.toDomain(entity);
    }

    @Override
    public void delete(ReviewOwnerReply reviewOwnerReply) {
        reviewOwnerReplyJpaRepository.deleteById(reviewOwnerReply.getId());
    }
}
