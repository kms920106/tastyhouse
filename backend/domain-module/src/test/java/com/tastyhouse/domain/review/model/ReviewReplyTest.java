package com.tastyhouse.domain.review.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;

import static org.assertj.core.api.Assertions.assertThat;
import com.tastyhouse.domain.review.vo.ReviewCommentId;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다.
 */
class ReviewReplyTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 숨김 처리되지 않은 상태다")
    void of_createsTransientReply() {
        ReviewReply reply = ReviewReply.of(ReviewCommentId.of(1L), MemberId.of(2L), MemberId.of(3L), "답글입니다");

        assertThat(reply.getId()).isNull();
        assertThat(reply.getCommentId()).isEqualTo(ReviewCommentId.of(1L));
        assertThat(reply.getMemberId()).isEqualTo(MemberId.of(2L));
        assertThat(reply.getReplyToMemberId()).isEqualTo(MemberId.of(3L));
        assertThat(reply.getContent()).isEqualTo("답글입니다");
        assertThat(reply.isHidden()).isFalse();
        assertThat(reply.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("of에서 replyToMemberId가 null이어도 생성된다(선택 필드)")
    void of_allowsNullReplyToMemberId() {
        ReviewReply reply = ReviewReply.of(ReviewCommentId.of(1L), MemberId.of(2L), null, "답글입니다");

        assertThat(reply.getReplyToMemberId()).isNull();
    }

    @Test
    @DisplayName("hide/unhide는 숨김 플래그를 전환한다")
    void hideUnhide_togglesHidden() {
        ReviewReply reply = ReviewReply.of(ReviewCommentId.of(1L), MemberId.of(2L), MemberId.of(3L), "답글입니다");

        reply.hide();
        assertThat(reply.isHidden()).isTrue();

        reply.unhide();
        assertThat(reply.isHidden()).isFalse();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각·숨김 상태를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);

        ReviewReply reply = ReviewReply.reconstitute(
            7L, ReviewCommentId.of(1L), MemberId.of(2L), MemberId.of(3L), "답글입니다", true, createdAt
        );

        assertThat(reply.getId()).isEqualTo(7L);
        assertThat(reply.isHidden()).isTrue();
        assertThat(reply.getCreatedAt()).isEqualTo(createdAt);
    }
}
