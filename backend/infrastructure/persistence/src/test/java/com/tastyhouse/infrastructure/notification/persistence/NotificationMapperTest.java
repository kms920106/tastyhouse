package com.tastyhouse.infrastructure.notification.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.notification.model.Notification;
import com.tastyhouse.domain.notification.model.NotificationTargetType;
import com.tastyhouse.domain.notification.model.NotificationType;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationMapperTest {

    @Test
    @DisplayName("Notification → 엔티티 변환 시 모든 컬럼 값이 옮겨진다")
    void toEntityCopiesColumns() {
        NotificationJpaEntity entity = NotificationMapper.toEntity(original());

        assertThat(entity.getMemberId()).isEqualTo(82L);
        assertThat(entity.getType()).isEqualTo("REVIEW_BLIND_APPROVED");
        assertThat(entity.getTitle()).isEqualTo("알림 제목");
        assertThat(entity.getBody()).isEqualTo("알림 본문");
        assertThat(entity.getTargetType()).isEqualTo("REVIEW");
        assertThat(entity.getTargetId()).isEqualTo(83L);
        assertThat(entity.isRead()).isTrue();
        assertThat(entity.getReadAt()).isEqualTo(LocalDateTime.of(2026, 5, 1, 10, 0));
    }

    @Test
    @DisplayName("엔티티 → Notification 변환 시 id·생성일·수정일을 포함한 모든 필드가 복원된다")
    void toDomainRestoresAllFields() {
        Notification original = original();
        NotificationJpaEntity entity = NotificationMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", original.getId());
        ReflectionTestUtils.setField(entity, "createdAt", original.getCreatedAt());
        ReflectionTestUtils.setField(entity, "updatedAt", original.getUpdatedAt());

        Notification restored = NotificationMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("applyChanges는 읽음 여부와 읽은 시각을 옮긴다")
    void applyChangesCopiesWritableFields() {
        NotificationJpaEntity entity = NotificationMapper.toEntity(Notification.reconstitute(
            81L, MemberId.of(82L), NotificationType.REVIEW_BLIND_APPROVED,
            "알림 제목", "알림 본문", NotificationTargetType.REVIEW, 83L, false, null, null, null));

        NotificationMapper.applyChanges(entity, original());

        assertThat(entity.isRead()).isTrue();
        assertThat(entity.getReadAt()).isEqualTo(LocalDateTime.of(2026, 5, 1, 10, 0));
    }

    private static Notification original() {
        return Notification.reconstitute(
            81L, MemberId.of(82L), NotificationType.REVIEW_BLIND_APPROVED,
            "알림 제목", "알림 본문", NotificationTargetType.REVIEW, 83L, true,
            LocalDateTime.of(2026, 5, 1, 10, 0),
            LocalDateTime.of(2026, 5, 2, 11, 0),
            LocalDateTime.of(2026, 5, 3, 12, 0));
    }
}
