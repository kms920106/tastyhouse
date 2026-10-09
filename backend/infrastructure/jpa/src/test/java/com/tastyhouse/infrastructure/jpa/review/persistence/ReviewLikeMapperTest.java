package com.tastyhouse.infrastructure.jpa.review.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewLike;
import com.tastyhouse.domain.review.vo.ReviewId;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewLikeMapperTest {

    @Test
    @DisplayName("ReviewLike → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void toEntity() {
        ReviewLikeJpaEntity entity = ReviewLikeMapper.toEntity(reviewLike());

        assertThat(entity.getReviewId()).isEqualTo(122L);
        assertThat(entity.getMemberId()).isEqualTo(123L);
    }

    @Test
    @DisplayName("엔티티 → ReviewLike 변환 시 모든 필드가 보존된다")
    void toDomain() {
        ReviewLike original = reviewLike();
        ReviewLikeJpaEntity entity = ReviewLikeMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", original.getId());
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 7, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 7, 2, 0, 0));

        ReviewLike restored = ReviewLikeMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static ReviewLike reviewLike() {
        return ReviewLike.reconstitute(121L, ReviewId.of(122L), MemberId.of(123L));
    }
}
