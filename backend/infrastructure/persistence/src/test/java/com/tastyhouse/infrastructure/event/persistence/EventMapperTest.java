package com.tastyhouse.infrastructure.event.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.event.model.Event;
import com.tastyhouse.domain.event.model.EventAnnouncement;
import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.domain.event.model.EventWinner;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.vo.PhoneNumber;

import static org.assertj.core.api.Assertions.assertThat;

class EventMapperTest {

    @Test
    @DisplayName("Event → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void eventToEntity() {
        Event original = event();

        EventJpaEntity entity = EventMapper.toEntity(original);

        assertThat(entity.getName()).isEqualTo("이벤트");
        assertThat(entity.getDescription()).isEqualTo("설명");
        assertThat(entity.getSubtitle()).isEqualTo("부제");
        assertThat(entity.getThumbnailImageFileId()).isEqualTo(62L);
        assertThat(entity.getBannerImageFileId()).isEqualTo(63L);
        assertThat(entity.getContentHtml()).isEqualTo("<p>html</p>");
        assertThat(entity.getStatus()).isEqualTo("ENDED");
        assertThat(entity.getStartAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertThat(entity.getEndAt()).isEqualTo(LocalDateTime.of(2026, 2, 1, 0, 0));
        assertThat(entity.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("엔티티 → Event 변환 시 id·생성·수정 시각을 포함한 모든 필드가 보존된다")
    void eventToDomain() {
        Event original = event();
        EventJpaEntity entity = EventMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", 61L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 3, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 4, 1, 0, 0));

        Event restored = EventMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("EventAnnouncement → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void eventAnnouncementToEntity() {
        EventAnnouncementJpaEntity entity = EventAnnouncementMapper.toEntity(eventAnnouncement());

        assertThat(entity.getEventId()).isEqualTo(72L);
        assertThat(entity.getName()).isEqualTo("발표명");
        assertThat(entity.getContent()).isEqualTo("발표 내용");
        assertThat(entity.getAnnouncedAt()).isEqualTo(LocalDateTime.of(2026, 5, 1, 0, 0));
    }

    @Test
    @DisplayName("엔티티 → EventAnnouncement 변환 시 id를 포함한 모든 필드가 보존된다")
    void eventAnnouncementToDomain() {
        EventAnnouncement original = eventAnnouncement();
        EventAnnouncementJpaEntity entity = EventAnnouncementMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", 71L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 5, 2, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 5, 3, 0, 0));

        EventAnnouncement restored = EventAnnouncementMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("EventWinner → 엔티티 변환 시 PhoneNumber를 포함한 모든 컬럼 값이 보존된다")
    void eventWinnerToEntity() {
        EventWinnerJpaEntity entity = EventWinnerMapper.toEntity(eventWinner());

        assertThat(entity.getEventId()).isEqualTo(82L);
        assertThat(entity.getRankNo()).isEqualTo(3);
        assertThat(entity.getWinnerName()).isEqualTo("당첨자");
        assertThat(entity.getPhoneNumber().value()).isEqualTo("01012345678");
        assertThat(entity.getAnnouncedAt()).isEqualTo(LocalDateTime.of(2026, 6, 1, 0, 0));
        assertThat(entity.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("엔티티 → EventWinner 변환 시 PhoneNumber를 포함한 모든 필드가 보존된다")
    void eventWinnerToDomain() {
        EventWinner original = eventWinner();
        EventWinnerJpaEntity entity = EventWinnerMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", 81L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 6, 2, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 6, 3, 0, 0));

        EventWinner restored = EventWinnerMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static Event event() {
        return Event.reconstitute(
            61L, "이벤트", "설명", "부제", UploadedFileId.of(62L), UploadedFileId.of(63L), "<p>html</p>",
            EventStatus.ENDED,
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 2, 1, 0, 0),
            true,
            LocalDateTime.of(2026, 3, 1, 0, 0),
            LocalDateTime.of(2026, 4, 1, 0, 0));
    }

    private static EventAnnouncement eventAnnouncement() {
        return EventAnnouncement.reconstitute(
            71L, EventId.of(72L), "발표명", "발표 내용", LocalDateTime.of(2026, 5, 1, 0, 0));
    }

    private static EventWinner eventWinner() {
        return EventWinner.reconstitute(
            81L, EventId.of(82L), 3, "당첨자", new PhoneNumber("01012345678"),
            LocalDateTime.of(2026, 6, 1, 0, 0), true);
    }
}
