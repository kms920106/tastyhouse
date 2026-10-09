package com.tastyhouse.infrastructure.jpa.review.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewReply;
import com.tastyhouse.domain.review.vo.ReviewCommentId;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewReplyMapperTest {

    @Test
    @DisplayName("ReviewReply → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void toEntity() {
        ReviewReplyJpaEntity entity = ReviewReplyMapper.toEntity(reply());

        assertThat(entity.getCommentId()).isEqualTo(142L);
        assertThat(entity.getMemberId()).isEqualTo(143L);
        assertThat(entity.getReplyToMemberId()).isEqualTo(144L);
        assertThat(entity.getContent()).isEqualTo("답글 내용");
        assertThat(entity.isHidden()).isTrue();
    }

    @Test
    @DisplayName("엔티티 → ReviewReply 변환 시 모든 필드가 보존된다")
    void toDomain() {
        ReviewReply original = reply();
        ReviewReplyJpaEntity entity = ReviewReplyMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", original.getId());
        ReflectionTestUtils.setField(entity, "createdAt", original.getCreatedAt());
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 7, 4, 11, 0));

        ReviewReply restored = ReviewReplyMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static ReviewReply reply() {
        return ReviewReply.reconstitute(
            141L, ReviewCommentId.of(142L), MemberId.of(143L), MemberId.of(144L), "답글 내용", true,
            LocalDateTime.of(2026, 7, 3, 11, 0));
    }
}
