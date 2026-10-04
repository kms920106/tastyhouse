package com.tastyhouse.infrastructure.persistence.review.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.review.model.ReviewOwnerReply;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewOwnerReplyMapperTest {

    @Test
    @DisplayName("ReviewOwnerReply → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void toEntity() {
        ReviewOwnerReplyJpaEntity entity = ReviewOwnerReplyMapper.toEntity(ownerReply());

        assertThat(entity.getReviewId()).isEqualTo(152L);
        assertThat(entity.getShopId()).isEqualTo(153L);
        assertThat(entity.getCeoId()).isEqualTo(154L);
        assertThat(entity.getContent()).isEqualTo("사장님 답변");
    }

    @Test
    @DisplayName("엔티티 → ReviewOwnerReply 변환 시 모든 필드가 보존된다")
    void toDomain() {
        ReviewOwnerReply original = ownerReply();
        ReviewOwnerReplyJpaEntity entity = ReviewOwnerReplyMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", original.getId());
        ReflectionTestUtils.setField(entity, "createdAt", original.getCreatedAt());
        ReflectionTestUtils.setField(entity, "updatedAt", original.getUpdatedAt());

        ReviewOwnerReply restored = ReviewOwnerReplyMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static ReviewOwnerReply ownerReply() {
        return ReviewOwnerReply.reconstitute(
            151L, ReviewId.of(152L), ShopId.of(153L), CeoId.of(154L), "사장님 답변",
            LocalDateTime.of(2026, 7, 4, 12, 0), LocalDateTime.of(2026, 7, 5, 13, 0));
    }
}
