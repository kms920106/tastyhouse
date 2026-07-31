package com.tastyhouse.domain.review.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.domain.vo.MemberId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 불변 애그리거트 단위 테스트. of/reconstitute 왕복만 검증한다(상태전이 없음).
 */
class ReviewLikeTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자 없음)다")
    void of_createsTransientLike() {
        ReviewLike like = ReviewLike.of(1L, MemberId.of(2L));

        assertThat(like.getId()).isNull();
        assertThat(like.getReviewId()).isEqualTo(1L);
        assertThat(like.getMemberId()).isEqualTo(MemberId.of(2L));
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ReviewLike like = ReviewLike.reconstitute(3L, 1L, MemberId.of(2L));

        assertThat(like.getId()).isEqualTo(3L);
        assertThat(like.getReviewId()).isEqualTo(1L);
        assertThat(like.getMemberId()).isEqualTo(MemberId.of(2L));
    }
}
