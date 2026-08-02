package com.tastyhouse.domain.review.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.tastyhouse.domain.review.domain.vo.ReviewId;
import com.tastyhouse.domain.shop.domain.vo.TagId;

/**
 * 불변 애그리거트 단위 테스트. of/reconstitute 왕복만 검증한다(상태전이 없음).
 */
class ReviewTagTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자 없음)다")
    void of_createsTransientTag() {
        ReviewTag tag = ReviewTag.of(ReviewId.of(1L), TagId.of(50L));

        assertThat(tag.getId()).isNull();
        assertThat(tag.getReviewId()).isEqualTo(ReviewId.of(1L));
        assertThat(tag.getTagId()).isEqualTo(TagId.of(50L));
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ReviewTag tag = ReviewTag.reconstitute(4L, ReviewId.of(1L), TagId.of(50L));

        assertThat(tag.getId()).isEqualTo(4L);
        assertThat(tag.getReviewId()).isEqualTo(ReviewId.of(1L));
        assertThat(tag.getTagId()).isEqualTo(TagId.of(50L));
    }
}
