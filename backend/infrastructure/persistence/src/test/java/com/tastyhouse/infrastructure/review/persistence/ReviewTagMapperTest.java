package com.tastyhouse.infrastructure.review.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.review.model.ReviewTag;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.TagId;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewTagMapperTest {

    @Test
    @DisplayName("ReviewTag → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void toEntity() {
        ReviewTag original = ReviewTag.reconstitute(161L, ReviewId.of(162L), TagId.of(163L));

        ReviewTagJpaEntity entity = ReviewTagMapper.toEntity(original);

        assertThat(entity.getReviewId()).isEqualTo(162L);
        assertThat(ReflectionTestUtils.getField(entity, "tagId")).isEqualTo(163L);
    }
}
