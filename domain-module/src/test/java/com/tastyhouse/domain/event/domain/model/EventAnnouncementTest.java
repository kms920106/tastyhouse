package com.tastyhouse.domain.event.domain.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class EventAnnouncementTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자 없음)다")
    void of_createsTransientEventAnnouncement() {
        LocalDateTime announcedAt = LocalDateTime.of(2026, 1, 1, 0, 0);

        EventAnnouncement announcement = EventAnnouncement.of(1L, "제목", "내용", announcedAt);

        assertThat(announcement.getId()).isNull();
        assertThat(announcement.getEventId()).isEqualTo(1L);
        assertThat(announcement.getName()).isEqualTo("제목");
        assertThat(announcement.getContent()).isEqualTo("내용");
        assertThat(announcement.getAnnouncedAt()).isEqualTo(announcedAt);
    }

    @Test
    @DisplayName("update는 제목·내용·발표일시를 변경한다")
    void update_changesFields() {
        LocalDateTime announcedAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        EventAnnouncement announcement = EventAnnouncement.of(1L, "제목", "내용", announcedAt);

        LocalDateTime newAnnouncedAt = LocalDateTime.of(2026, 2, 1, 0, 0);
        announcement.update("새 제목", "새 내용", newAnnouncedAt);

        assertThat(announcement.getName()).isEqualTo("새 제목");
        assertThat(announcement.getContent()).isEqualTo("새 내용");
        assertThat(announcement.getAnnouncedAt()).isEqualTo(newAnnouncedAt);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime announcedAt = LocalDateTime.of(2026, 1, 1, 0, 0);

        EventAnnouncement announcement = EventAnnouncement.reconstitute(1L, 2L, "제목", "내용", announcedAt);

        assertThat(announcement.getId()).isEqualTo(1L);
        assertThat(announcement.getEventId()).isEqualTo(2L);
        assertThat(announcement.getName()).isEqualTo("제목");
        assertThat(announcement.getContent()).isEqualTo("내용");
        assertThat(announcement.getAnnouncedAt()).isEqualTo(announcedAt);
    }
}
