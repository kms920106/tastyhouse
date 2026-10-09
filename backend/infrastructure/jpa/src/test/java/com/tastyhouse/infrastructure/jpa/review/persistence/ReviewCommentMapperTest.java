package com.tastyhouse.infrastructure.jpa.review.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewComment;
import com.tastyhouse.domain.review.vo.ReviewId;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewCommentMapperTest {

    @Test
    @DisplayName("ReviewComment → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void toEntity() {
        ReviewCommentJpaEntity entity = ReviewCommentMapper.toEntity(comment());

        assertThat(entity.getReviewId()).isEqualTo(132L);
        assertThat(entity.getMemberId()).isEqualTo(133L);
        assertThat(entity.getContent()).isEqualTo("댓글 내용");
        assertThat(entity.isHidden()).isTrue();
    }

    @Test
    @DisplayName("엔티티 → ReviewComment 변환 시 모든 필드가 보존된다")
    void toDomain() {
        ReviewComment original = comment();
        ReviewCommentJpaEntity entity = ReviewCommentMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", original.getId());
        ReflectionTestUtils.setField(entity, "createdAt", original.getCreatedAt());
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 7, 3, 10, 0));

        ReviewComment restored = ReviewCommentMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static ReviewComment comment() {
        return ReviewComment.reconstitute(
            131L, ReviewId.of(132L), MemberId.of(133L), "댓글 내용", true, LocalDateTime.of(2026, 7, 2, 10, 0));
    }
}
