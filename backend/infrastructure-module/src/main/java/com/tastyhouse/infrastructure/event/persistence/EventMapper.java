package com.tastyhouse.infrastructure.event.persistence;

import com.tastyhouse.domain.event.model.Event;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 이벤트 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class EventMapper {

    private EventMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static Event toDomain(EventJpaEntity entity) {
        return Event.reconstitute(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getSubtitle(),
            IdMapping.vo(entity.getThumbnailImageFileId(), UploadedFileId::of),
            IdMapping.vo(entity.getBannerImageFileId(), UploadedFileId::of),
            entity.getContentHtml(),
            entity.getStatus(),
            entity.getStartAt(),
            entity.getEndAt(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static EventJpaEntity toEntity(Event domain) {
        return EventJpaEntity.create(
            domain.getName(),
            domain.getDescription(),
            domain.getSubtitle(),
            IdMapping.raw(domain.getThumbnailImageFileId(), UploadedFileId::value),
            IdMapping.raw(domain.getBannerImageFileId(), UploadedFileId::value),
            domain.getContentHtml(),
            domain.getStatus(),
            domain.getStartAt(),
            domain.getEndAt(),
            domain.isDeleted()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(EventJpaEntity entity, Event domain) {
        entity.applyChanges(
            domain.getName(),
            domain.getDescription(),
            domain.getSubtitle(),
            IdMapping.raw(domain.getThumbnailImageFileId(), UploadedFileId::value),
            IdMapping.raw(domain.getBannerImageFileId(), UploadedFileId::value),
            domain.getContentHtml(),
            domain.getStatus(),
            domain.getStartAt(),
            domain.getEndAt(),
            domain.isDeleted()
        );
    }
}
