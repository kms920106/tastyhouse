package com.tastyhouse.domain.review.domain.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.review.model.ReviewComment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.vo.ReviewCommentId;

import static org.assertj.core.api.Assertions.assertThat;
import com.tastyhouse.domain.review.vo.ReviewId;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다.
 */
class ReviewCommentTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 숨김 처리되지 않은 상태다")
    void of_createsTransientComment() {
        ReviewComment comment = ReviewComment.of(ReviewId.of(1L), MemberId.of(2L), "좋은 리뷰네요");

        assertThat(comment.getId()).isNull();
        assertThat(comment.getReviewId()).isEqualTo(ReviewId.of(1L));
        assertThat(comment.getMemberId()).isEqualTo(MemberId.of(2L));
        assertThat(comment.getContent()).isEqualTo("좋은 리뷰네요");
        assertThat(comment.isHidden()).isFalse();
        assertThat(comment.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("hide/unhide는 숨김 플래그를 전환한다")
    void hideUnhide_togglesHidden() {
        ReviewComment comment = ReviewComment.of(ReviewId.of(1L), MemberId.of(2L), "좋은 리뷰네요");

        comment.hide();
        assertThat(comment.isHidden()).isTrue();

        comment.unhide();
        assertThat(comment.isHidden()).isFalse();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각·숨김 상태를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);

        ReviewComment comment = ReviewComment.reconstitute(
            5L, ReviewId.of(1L), MemberId.of(2L), "좋은 리뷰네요", true, createdAt
        );

        assertThat(comment.getId()).isEqualTo(5L);
        assertThat(comment.getReviewCommentId()).isEqualTo(ReviewCommentId.of(5L));
        assertThat(comment.isHidden()).isTrue();
        assertThat(comment.getCreatedAt()).isEqualTo(createdAt);
    }
}
