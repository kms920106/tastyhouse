package com.tastyhouse.domain.notice.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.notice.vo.NoticeId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class NoticeTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 삭제되지 않은 상태다")
    void of_createsTransientNotice() {
        Notice notice = Notice.of("제목", "내용", true);

        assertThat(notice.getId()).isNull();
        assertThat(notice.getTitle()).isEqualTo("제목");
        assertThat(notice.getContent()).isEqualTo("내용");
        assertThat(notice.isVisible()).isTrue();
        assertThat(notice.isDeleted()).isFalse();
        assertThat(notice.getCreatedAt()).isNull();
        assertThat(notice.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("update는 제목·내용·노출여부를 변경한다")
    void update_changesFields() {
        Notice notice = Notice.of("제목", "내용", true);

        notice.update("새 제목", "새 내용", false);

        assertThat(notice.getTitle()).isEqualTo("새 제목");
        assertThat(notice.getContent()).isEqualTo("새 내용");
        assertThat(notice.isVisible()).isFalse();
    }

    @Test
    @DisplayName("delete는 삭제 플래그를 true로 만든다(soft delete)")
    void delete_marksDeleted() {
        Notice notice = Notice.of("제목", "내용", true);

        notice.delete();

        assertThat(notice.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

        Notice notice = Notice.reconstitute(1L, "제목", "내용", true, false, createdAt, updatedAt);

        assertThat(notice.getId()).isEqualTo(1L);
        assertThat(notice.getNoticeId()).isEqualTo(NoticeId.of(1L));
        assertThat(notice.getCreatedAt()).isEqualTo(createdAt);
        assertThat(notice.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("미영속 상태에서 getNoticeId를 호출하면 NoticeId 불변식 위반으로 예외가 발생한다")
    void getNoticeId_onTransient_throws() {
        Notice notice = Notice.of("제목", "내용", true);

        assertThatThrownBy(notice::getNoticeId)
            .isInstanceOf(IllegalArgumentException.class);
    }
}
