package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopContentBoard;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 가게 콘텐츠보드 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class ShopContentBoardMapper {

    private ShopContentBoardMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ShopContentBoard toDomain(ShopContentBoardJpaEntity entity) {
        return ShopContentBoard.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            entity.getContentType(),
            entity.getTopic(),
            IdMapping.vo(entity.getImageFileId(), UploadedFileId::of),
            entity.getYoutubeUrl(),
            entity.getDescription(),
            entity.isHidden(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ShopContentBoardJpaEntity toEntity(ShopContentBoard domain) {
        return ShopContentBoardJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            domain.getContentType(),
            domain.getTopic(),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getYoutubeUrl(),
            domain.getDescription(),
            domain.isHidden()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(ShopContentBoardJpaEntity entity, ShopContentBoard domain) {
        entity.applyChanges(
            domain.getTopic(),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getYoutubeUrl(),
            domain.getDescription(),
            domain.isHidden()
        );
    }
}
