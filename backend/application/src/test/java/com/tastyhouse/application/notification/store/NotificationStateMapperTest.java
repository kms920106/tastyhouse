package com.tastyhouse.application.notification.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.notification.model.Notification;
import com.tastyhouse.domain.notification.model.NotificationTargetType;
import com.tastyhouse.domain.notification.model.NotificationType;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationStateMapperTest {

    @Test
    @DisplayName("Notification → NotificationState → Notification 왕복 시 모든 필드가 보존된다")
    void roundTrip() {
        Notification original = Notification.reconstitute(
            81L, MemberId.of(82L), NotificationType.REVIEW_BLIND_APPROVED,
            "알림 제목", "알림 본문", NotificationTargetType.REVIEW, 83L, true,
            LocalDateTime.of(2026, 5, 1, 10, 0),
            LocalDateTime.of(2026, 5, 2, 11, 0),
            LocalDateTime.of(2026, 5, 3, 12, 0));

        Notification restored = NotificationStateMapper.toDomain(NotificationStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
