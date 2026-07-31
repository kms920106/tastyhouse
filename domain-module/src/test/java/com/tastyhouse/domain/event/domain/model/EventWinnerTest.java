package com.tastyhouse.domain.event.domain.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shared.vo.PhoneNumber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class EventWinnerTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자 없음)이고 삭제되지 않은 상태다")
    void of_createsTransientEventWinner() {
        LocalDateTime announcedAt = LocalDateTime.of(2026, 1, 1, 0, 0);

        EventWinner winner = EventWinner.of(1L, 1, "당첨자", "01012345678", announcedAt);

        assertThat(winner.getId()).isNull();
        assertThat(winner.getEventId()).isEqualTo(1L);
        assertThat(winner.getRankNo()).isEqualTo(1);
        assertThat(winner.getWinnerName()).isEqualTo("당첨자");
        assertThat(winner.getPhoneNumber().value()).isEqualTo("01012345678");
        assertThat(winner.getAnnouncedAt()).isEqualTo(announcedAt);
        assertThat(winner.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("잘못된 휴대폰번호 형식이면 예외가 발생한다")
    void of_withInvalidPhoneNumber_throws() {
        LocalDateTime announcedAt = LocalDateTime.of(2026, 1, 1, 0, 0);

        assertThatThrownBy(() -> EventWinner.of(1L, 1, "당첨자", "invalid", announcedAt))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("delete는 삭제 플래그를 true로 만든다(soft delete)")
    void delete_marksDeleted() {
        LocalDateTime announcedAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        EventWinner winner = EventWinner.of(1L, 1, "당첨자", "01012345678", announcedAt);

        winner.delete();

        assertThat(winner.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime announcedAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        PhoneNumber phoneNumber = new PhoneNumber("01012345678");

        EventWinner winner = EventWinner.reconstitute(1L, 2L, 1, "당첨자", phoneNumber, announcedAt, false);

        assertThat(winner.getId()).isEqualTo(1L);
        assertThat(winner.getEventId()).isEqualTo(2L);
        assertThat(winner.getRankNo()).isEqualTo(1);
        assertThat(winner.getWinnerName()).isEqualTo("당첨자");
        assertThat(winner.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(winner.getAnnouncedAt()).isEqualTo(announcedAt);
        assertThat(winner.isDeleted()).isFalse();
    }
}
