package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopNoticeImage;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 점주 공지 첨부 이미지 도메인 모델 ↔ JPA 엔티티 변환기.
 *
 * <p>불변 애그리거트라 update 경로가 없어 {@code applyChanges}를 두지 않는다.
 */
final class ShopNoticeImageMapper {

    private ShopNoticeImageMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static ShopNoticeImage toDomain(ShopNoticeImageJpaEntity entity) {
        return ShopNoticeImage.reconstitute(
            entity.getId(),
            entity.getShopNoticeId(),
            IdMapping.vo(entity.getImageFileId(), UploadedFileId::of),
            entity.getSortOrder()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static ShopNoticeImageJpaEntity toEntity(ShopNoticeImage domain) {
        return ShopNoticeImageJpaEntity.create(
            domain.getShopNoticeId(),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.getSortOrder()
        );
    }
}
