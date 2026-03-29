package com.tastyhouse.core.repository.review;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.review.QReviewLike;
import com.tastyhouse.core.entity.review.ReviewLike;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ReviewLikeRepositoryImpl implements ReviewLikeRepository {

    private final JPAQueryFactory queryFactory;
    private final ReviewLikeJpaRepository reviewLikeJpaRepository;

    @Override
    public Map<Long, Long> countByReviewIdIn(List<Long> reviewIds) {
        QReviewLike reviewLike = QReviewLike.reviewLike;

        List<Tuple> results = queryFactory
            .select(reviewLike.reviewId, reviewLike.count())
            .from(reviewLike)
            .where(reviewLike.reviewId.in(reviewIds))
            .groupBy(reviewLike.reviewId)
            .fetch();

        Map<Long, Long> countMap = new HashMap<>();
        for (Tuple row : results) {
            countMap.put(row.get(0, Long.class), row.get(1, Long.class));
        }
        return countMap;
    }

    @Override
    public boolean existsByReviewIdAndMemberId(Long reviewId, Long memberId) {
        QReviewLike reviewLike = QReviewLike.reviewLike;
        return queryFactory
            .selectOne()
            .from(reviewLike)
            .where(
                reviewLike.reviewId.eq(reviewId),
                reviewLike.memberId.eq(memberId)
            )
            .fetchFirst() != null;
    }

    @Override
    public void deleteByReviewIdAndMemberId(Long reviewId, Long memberId) {
        QReviewLike reviewLike = QReviewLike.reviewLike;
        queryFactory
            .delete(reviewLike)
            .where(
                reviewLike.reviewId.eq(reviewId),
                reviewLike.memberId.eq(memberId)
            )
            .execute();
    }

    @Override
    public ReviewLike save(ReviewLike reviewLike) {
        return reviewLikeJpaRepository.save(reviewLike);
    }
}
