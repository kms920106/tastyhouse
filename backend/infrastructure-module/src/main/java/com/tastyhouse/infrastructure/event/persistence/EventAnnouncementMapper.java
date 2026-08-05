package com.tastyhouse.infrastructure.event.persistence;

import com.tastyhouse.domain.event.model.EventAnnouncement;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 이벤트 당첨자 발표 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class EventAnnouncementMapper {

    private EventAnnouncementMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static EventAnnouncement toDomain(EventAnnouncementJpaEntity entity) {
        return EventAnnouncement.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getEventId(), EventId::of),
            entity.getName(),
            entity.getContent(),
            entity.getAnnouncedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static EventAnnouncementJpaEntity toEntity(EventAnnouncement domain) {
        return EventAnnouncementJpaEntity.create(
            IdMapping.raw(domain.getEventId(), EventId::value),
            domain.getName(),
            domain.getContent(),
            domain.getAnnouncedAt()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(EventAnnouncementJpaEntity entity, EventAnnouncement domain) {
        entity.applyChanges(
            domain.getName(),
            domain.getContent(),
            domain.getAnnouncedAt()
        );
    }
}
