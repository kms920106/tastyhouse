package com.tastyhouse.domain.event.domain.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.event.domain.vo.EventId;
import com.tastyhouse.domain.file.domain.vo.UploadedFileId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class EventTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 삭제되지 않은 상태다")
    void of_createsTransientEvent() {
        LocalDateTime startAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime endAt = LocalDateTime.of(2026, 1, 31, 0, 0);

        Event event = Event.of(
            "이벤트명", "설명", "부제목", UploadedFileId.of(1L), UploadedFileId.of(2L), "<p>본문</p>", EventStatus.SCHEDULED, startAt, endAt
        );

        assertThat(event.getId()).isNull();
        assertThat(event.getName()).isEqualTo("이벤트명");
        assertThat(event.getDescription()).isEqualTo("설명");
        assertThat(event.getSubtitle()).isEqualTo("부제목");
        assertThat(event.getThumbnailImageFileId()).isEqualTo(UploadedFileId.of(1L));
        assertThat(event.getBannerImageFileId()).isEqualTo(UploadedFileId.of(2L));
        assertThat(event.getContentHtml()).isEqualTo("<p>본문</p>");
        assertThat(event.getStatus()).isEqualTo(EventStatus.SCHEDULED);
        assertThat(event.getStartAt()).isEqualTo(startAt);
        assertThat(event.getEndAt()).isEqualTo(endAt);
        assertThat(event.isDeleted()).isFalse();
        assertThat(event.getCreatedAt()).isNull();
        assertThat(event.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("update는 이벤트 필드 전체를 변경한다")
    void update_changesFields() {
        LocalDateTime startAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime endAt = LocalDateTime.of(2026, 1, 31, 0, 0);
        Event event = Event.of(
            "이벤트명", "설명", "부제목", UploadedFileId.of(1L), UploadedFileId.of(2L), "<p>본문</p>", EventStatus.SCHEDULED, startAt, endAt
        );

        LocalDateTime newStartAt = LocalDateTime.of(2026, 2, 1, 0, 0);
        LocalDateTime newEndAt = LocalDateTime.of(2026, 2, 28, 0, 0);
        event.update(
            "새 이벤트명", "새 설명", "새 부제목", UploadedFileId.of(3L), UploadedFileId.of(4L), "<p>새 본문</p>", EventStatus.ACTIVE, newStartAt, newEndAt
        );

        assertThat(event.getName()).isEqualTo("새 이벤트명");
        assertThat(event.getDescription()).isEqualTo("새 설명");
        assertThat(event.getSubtitle()).isEqualTo("새 부제목");
        assertThat(event.getThumbnailImageFileId()).isEqualTo(UploadedFileId.of(3L));
        assertThat(event.getBannerImageFileId()).isEqualTo(UploadedFileId.of(4L));
        assertThat(event.getContentHtml()).isEqualTo("<p>새 본문</p>");
        assertThat(event.getStatus()).isEqualTo(EventStatus.ACTIVE);
        assertThat(event.getStartAt()).isEqualTo(newStartAt);
        assertThat(event.getEndAt()).isEqualTo(newEndAt);
    }

    @Test
    @DisplayName("delete는 삭제 플래그를 true로 만든다(soft delete)")
    void delete_marksDeleted() {
        LocalDateTime startAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime endAt = LocalDateTime.of(2026, 1, 31, 0, 0);
        Event event = Event.of(
            "이벤트명", "설명", "부제목", UploadedFileId.of(1L), UploadedFileId.of(2L), "<p>본문</p>", EventStatus.SCHEDULED, startAt, endAt
        );

        event.delete();

        assertThat(event.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime startAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime endAt = LocalDateTime.of(2026, 1, 31, 0, 0);
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

        Event event = Event.reconstitute(
            1L, "이벤트명", "설명", "부제목", UploadedFileId.of(1L), UploadedFileId.of(2L), "<p>본문</p>", EventStatus.ACTIVE,
            startAt, endAt, false, createdAt, updatedAt
        );

        assertThat(event.getId()).isEqualTo(1L);
        assertThat(event.getEventId()).isEqualTo(EventId.of(1L));
        assertThat(event.getCreatedAt()).isEqualTo(createdAt);
        assertThat(event.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("미영속 상태에서 getEventId를 호출하면 EventId 불변식 위반으로 예외가 발생한다")
    void getEventId_onTransient_throws() {
        LocalDateTime startAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime endAt = LocalDateTime.of(2026, 1, 31, 0, 0);
        Event event = Event.of(
            "이벤트명", "설명", "부제목", UploadedFileId.of(1L), UploadedFileId.of(2L), "<p>본문</p>", EventStatus.SCHEDULED, startAt, endAt
        );

        assertThatThrownBy(event::getEventId)
            .isInstanceOf(IllegalArgumentException.class);
    }
}
