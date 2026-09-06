package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.vo.StationId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopMapper {
    private ShopMapper() {
    }

    static Shop toDomain(ShopJpaEntity entity) {
        return Shop.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getCeoId(), CeoId::of),
            IdMapping.vo(entity.getStationId(), StationId::of),
            entity.getName(),
            entity.getLatitude(),
            entity.getLongitude(),
            entity.getRating(),
            entity.getRoadAddress(),
            entity.getLotAddress(),
            entity.getPhoneNumber(),
            IdMapping.vo(entity.getThumbnailImageFileId(), UploadedFileId::of),
            IdMapping.vo(entity.getTrademarkImageFileId(), UploadedFileId::of),
            entity.isPermanentlyClosed(),
            entity.isHidden(),
            entity.isClosedOnPublicHolidays(),
            entity.getMinOrderAmount(),
            entity.isScheduledOrderEnabled(),
            entity.isCupDepositEnabled(),
            entity.isStorePriceVerified(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ShopJpaEntity toEntity(Shop domain) {
        return ShopJpaEntity.create(
            IdMapping.raw(domain.getCeoId(), CeoId::value),
            IdMapping.raw(domain.getStationId(), StationId::value),
            domain.getName(),
            domain.getLatitude(),
            domain.getLongitude(),
            domain.getRating(),
            domain.getRoadAddress(),
            domain.getLotAddress(),
            domain.getPhoneNumber(),
            IdMapping.raw(domain.getThumbnailImageFileId(), UploadedFileId::value),
            IdMapping.raw(domain.getTrademarkImageFileId(), UploadedFileId::value),
            domain.isPermanentlyClosed(),
            domain.isHidden(),
            domain.isClosedOnPublicHolidays(),
            domain.getMinOrderAmount(),
            domain.isScheduledOrderEnabled(),
            domain.isCupDepositEnabled(),
            domain.isStorePriceVerified()
        );
    }

    static void applyChanges(ShopJpaEntity entity, Shop domain) {
        entity.applyChanges(
            IdMapping.raw(domain.getCeoId(), CeoId::value),
            IdMapping.raw(domain.getStationId(), StationId::value),
            domain.getName(),
            domain.getLatitude(),
            domain.getLongitude(),
            domain.getRating(),
            domain.getRoadAddress(),
            domain.getLotAddress(),
            domain.getPhoneNumber(),
            IdMapping.raw(domain.getThumbnailImageFileId(), UploadedFileId::value),
            IdMapping.raw(domain.getTrademarkImageFileId(), UploadedFileId::value),
            domain.isPermanentlyClosed(),
            domain.isHidden(),
            domain.isClosedOnPublicHolidays(),
            domain.getMinOrderAmount(),
            domain.isScheduledOrderEnabled(),
            domain.isCupDepositEnabled(),
            domain.isStorePriceVerified()
        );
    }
}
