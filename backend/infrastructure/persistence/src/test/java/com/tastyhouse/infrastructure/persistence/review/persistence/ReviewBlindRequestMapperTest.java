package com.tastyhouse.infrastructure.persistence.review.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.model.ReviewBlindStatus;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewBlindRequestMapperTest {

    @Test
    @DisplayName("ReviewBlindRequest → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void toEntity() {
        ReviewBlindRequestJpaEntity entity = ReviewBlindRequestMapper.toEntity(blindRequest());

        assertThat(entity.getReviewId()).isEqualTo(172L);
        assertThat(entity.getShopId()).isEqualTo(173L);
        assertThat(entity.getCeoId()).isEqualTo(174L);
        assertThat(entity.getReason()).isEqualTo("PRIVACY");
        assertThat(entity.getDetailReason()).isEqualTo("상세 사유");
        assertThat(entity.getStatus()).isEqualTo("APPROVED");
        assertThat(entity.getRejectReason()).isEqualTo("반려 사유");
        assertThat(entity.getBlindUntil()).isEqualTo(LocalDateTime.of(2026, 7, 6, 14, 0));
    }

    @Test
    @DisplayName("엔티티 → ReviewBlindRequest 변환 시 모든 필드가 보존된다")
    void toDomain() {
        ReviewBlindRequest original = blindRequest();
        ReviewBlindRequestJpaEntity entity = ReviewBlindRequestMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", original.getId());
        ReflectionTestUtils.setField(entity, "createdAt", original.getCreatedAt());
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 7, 8, 15, 0));

        ReviewBlindRequest restored = ReviewBlindRequestMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static ReviewBlindRequest blindRequest() {
        return ReviewBlindRequest.reconstitute(
            171L, ReviewId.of(172L), ShopId.of(173L), CeoId.of(174L), ReviewBlindReason.PRIVACY, "상세 사유",
            ReviewBlindStatus.APPROVED, "반려 사유", LocalDateTime.of(2026, 7, 6, 14, 0),
            LocalDateTime.of(2026, 7, 7, 15, 0));
    }
}
