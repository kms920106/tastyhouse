package com.tastyhouse.application.event.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.event.model.Event;
import com.tastyhouse.domain.event.model.EventAnnouncement;
import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.domain.event.model.EventWinner;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.vo.PhoneNumber;

import static org.assertj.core.api.Assertions.assertThat;

class EventStateMapperTest {

    @Test
    @DisplayName("Event → EventState → Event 왕복 시 모든 필드가 보존된다")
    void eventRoundTrip() {
        Event original = Event.reconstitute(
            61L, "이벤트", "설명", "부제", UploadedFileId.of(62L), UploadedFileId.of(63L), "<p>html</p>",
            EventStatus.ENDED,
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 2, 1, 0, 0),
            true,
            LocalDateTime.of(2026, 3, 1, 0, 0),
            LocalDateTime.of(2026, 4, 1, 0, 0));

        Event restored = EventStateMapper.toDomain(EventStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("EventAnnouncement → EventAnnouncementState → EventAnnouncement 왕복 시 모든 필드가 보존된다")
    void eventAnnouncementRoundTrip() {
        EventAnnouncement original = EventAnnouncement.reconstitute(
            71L, EventId.of(72L), "발표명", "발표 내용", LocalDateTime.of(2026, 5, 1, 0, 0));

        EventAnnouncement restored = EventAnnouncementStateMapper.toDomain(EventAnnouncementStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("EventWinner → EventWinnerState → EventWinner 왕복 시 PhoneNumber를 포함한 모든 필드가 보존된다")
    void eventWinnerRoundTrip() {
        EventWinner original = EventWinner.reconstitute(
            81L, EventId.of(82L), 3, "당첨자", new PhoneNumber("01012345678"),
            LocalDateTime.of(2026, 6, 1, 0, 0), true);

        EventWinner restored = EventWinnerStateMapper.toDomain(EventWinnerStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
