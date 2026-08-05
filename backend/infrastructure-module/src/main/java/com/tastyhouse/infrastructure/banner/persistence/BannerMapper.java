package com.tastyhouse.infrastructure.banner.persistence;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 배너 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class BannerMapper {

    private BannerMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static Banner toDomain(BannerJpaEntity entity) {
        return Banner.reconstitute(
            entity.getId(),
            entity.getType(),
            entity.getTitle(),
            IdMapping.vo(entity.getImageFileId(), UploadedFileId::of),
            entity.getLinkUrl(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getSort(),
            entity.isVisible(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static BannerJpaEntity toEntity(Banner domain) {
        return BannerJpaEntity.create(
            domain.getType(),
            domain.getTitle(),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getLinkUrl(),
            domain.getStartDate(),
            domain.getEndDate(),
            domain.getSort(),
            domain.isVisible(),
            domain.isDeleted()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(BannerJpaEntity entity, Banner domain) {
        entity.applyChanges(
            domain.getType(),
            domain.getTitle(),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getLinkUrl(),
            domain.getStartDate(),
            domain.getEndDate(),
            domain.getSort(),
            domain.isVisible(),
            domain.isDeleted()
        );
    }
}
