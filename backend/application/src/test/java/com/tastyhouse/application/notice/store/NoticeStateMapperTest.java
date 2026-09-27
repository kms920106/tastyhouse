package com.tastyhouse.application.notice.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.notice.model.Notice;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeStateMapperTest {

    @Test
    @DisplayName("Notice → NoticeState → Notice 왕복 시 모든 필드가 보존된다")
    void roundTrip() {
        Notice original = Notice.reconstitute(
            7L, "제목", "본문", true, false,
            LocalDateTime.of(2026, 1, 2, 3, 4, 5),
            LocalDateTime.of(2026, 6, 7, 8, 9, 10));

        Notice restored = NoticeStateMapper.toDomain(NoticeStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("boolean 필드가 뒤바뀌지 않는다")
    void booleanFieldsAreNotSwapped() {
        Notice original = Notice.reconstitute(
            8L, "t", "c", false, true,
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 2, 1, 0, 0));

        Notice restored = NoticeStateMapper.toDomain(NoticeStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
