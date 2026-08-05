package com.tastyhouse.infrastructure.rank.persistence;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.rank.model.RankPrize;
import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * 랭킹 경품 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class RankPrizeMapper {

    private RankPrizeMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static RankPrize toDomain(RankPrizeJpaEntity entity) {
        return RankPrize.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getRankId(), RankPeriodId::of),
            entity.getPrizeRank(),
            entity.getName(),
            entity.getBrand(),
            IdMapping.vo(entity.getImageFileId(), UploadedFileId::of),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static RankPrizeJpaEntity toEntity(RankPrize domain) {
        return RankPrizeJpaEntity.create(
            IdMapping.raw(domain.getRankId(), RankPeriodId::value),
            domain.getPrizeRank(),
            domain.getName(),
            domain.getBrand(),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.isDeleted()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(RankPrizeJpaEntity entity, RankPrize domain) {
        entity.applyChanges(
            domain.getPrizeRank(),
            domain.getName(),
            domain.getBrand(),
            IdMapping.raw(domain.getImageFileId(), UploadedFileId::value),
            domain.isDeleted()
        );
    }
}
