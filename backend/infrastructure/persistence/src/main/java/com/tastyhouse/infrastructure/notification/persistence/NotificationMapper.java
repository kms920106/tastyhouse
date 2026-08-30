package com.tastyhouse.infrastructure.notification.persistence;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.notification.model.Notification;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 알림 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class NotificationMapper {

    private NotificationMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static Notification toDomain(NotificationJpaEntity entity) {
        return Notification.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getMemberId(), MemberId::of),
            entity.getType(),
            entity.getTitle(),
            entity.getBody(),
            entity.getTargetType(),
            entity.getTargetId(),
            entity.isRead(),
            entity.getReadAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static NotificationJpaEntity toEntity(Notification domain) {
        return NotificationJpaEntity.create(
            IdMapping.raw(domain.getMemberId(), MemberId::value),
            domain.getType(),
            domain.getTitle(),
            domain.getBody(),
            domain.getTargetType(),
            domain.getTargetId(),
            domain.isRead(),
            domain.getReadAt()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(NotificationJpaEntity entity, Notification domain) {
        entity.applyChanges(domain.isRead(), domain.getReadAt());
    }
}
