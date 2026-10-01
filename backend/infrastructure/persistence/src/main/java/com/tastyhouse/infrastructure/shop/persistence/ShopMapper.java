package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.vo.StationId;

final class ShopMapper {

    private ShopMapper() {
    }

    static Shop toDomain(ShopJpaEntity entity) {
        return Shop.reconstitute(
            entity.getId(),
            entity.getCeoId() == null ? null : CeoId.of(entity.getCeoId()),
            entity.getStationId() == null ? null : StationId.of(entity.getStationId()),
            entity.getName(),
            entity.getLatitude(),
            entity.getLongitude(),
            entity.getRating(),
            entity.getRoadAddress(),
            entity.getLotAddress(),
            entity.getPhoneNumber(),
            entity.getThumbnailImageFileId() == null ? null : UploadedFileId.of(entity.getThumbnailImageFileId()),
            entity.getTrademarkImageFileId() == null ? null : UploadedFileId.of(entity.getTrademarkImageFileId()),
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

    static ShopJpaEntity toEntity(Shop shop) {
        return ShopJpaEntity.create(
            shop.getCeoId() == null ? null : shop.getCeoId().value(),
            shop.getStationId() == null ? null : shop.getStationId().value(),
            shop.getName(),
            shop.getLatitude(),
            shop.getLongitude(),
            shop.getRating(),
            shop.getRoadAddress(),
            shop.getLotAddress(),
            shop.getPhoneNumber(),
            shop.getThumbnailImageFileId() == null ? null : shop.getThumbnailImageFileId().value(),
            shop.getTrademarkImageFileId() == null ? null : shop.getTrademarkImageFileId().value(),
            shop.isPermanentlyClosed(),
            shop.isHidden(),
            shop.isClosedOnPublicHolidays(),
            shop.getMinOrderAmount(),
            shop.isScheduledOrderEnabled(),
            shop.isCupDepositEnabled(),
            shop.isStorePriceVerified()
        );
    }

    static void applyChanges(ShopJpaEntity entity, Shop shop) {
        entity.applyChanges(
            shop.getCeoId() == null ? null : shop.getCeoId().value(),
            shop.getStationId() == null ? null : shop.getStationId().value(),
            shop.getName(),
            shop.getLatitude(),
            shop.getLongitude(),
            shop.getRating(),
            shop.getRoadAddress(),
            shop.getLotAddress(),
            shop.getPhoneNumber(),
            shop.getThumbnailImageFileId() == null ? null : shop.getThumbnailImageFileId().value(),
            shop.getTrademarkImageFileId() == null ? null : shop.getTrademarkImageFileId().value(),
            shop.isPermanentlyClosed(),
            shop.isHidden(),
            shop.isClosedOnPublicHolidays(),
            shop.getMinOrderAmount(),
            shop.isScheduledOrderEnabled(),
            shop.isCupDepositEnabled(),
            shop.isStorePriceVerified()
        );
    }
}
