package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopState;

final class ShopMapper {
    private ShopMapper() {
    }

    static ShopState toState(ShopJpaEntity entity) {
        return new ShopState(
            entity.getId(),
            entity.getCeoId(),
            entity.getStationId(),
            entity.getName(),
            entity.getLatitude(),
            entity.getLongitude(),
            entity.getRating(),
            entity.getRoadAddress(),
            entity.getLotAddress(),
            entity.getPhoneNumber(),
            entity.getThumbnailImageFileId(),
            entity.getTrademarkImageFileId(),
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

    static ShopJpaEntity toEntity(ShopState state) {
        return ShopJpaEntity.create(
            state.ceoId(),
            state.stationId(),
            state.name(),
            state.latitude(),
            state.longitude(),
            state.rating(),
            state.roadAddress(),
            state.lotAddress(),
            state.phoneNumber(),
            state.thumbnailImageFileId(),
            state.trademarkImageFileId(),
            state.permanentlyClosed(),
            state.hidden(),
            state.closedOnPublicHolidays(),
            state.minOrderAmount(),
            state.scheduledOrderEnabled(),
            state.cupDepositEnabled(),
            state.storePriceVerified()
        );
    }

    static void applyChanges(ShopJpaEntity entity, ShopState state) {
        entity.applyChanges(
            state.ceoId(),
            state.stationId(),
            state.name(),
            state.latitude(),
            state.longitude(),
            state.rating(),
            state.roadAddress(),
            state.lotAddress(),
            state.phoneNumber(),
            state.thumbnailImageFileId(),
            state.trademarkImageFileId(),
            state.permanentlyClosed(),
            state.hidden(),
            state.closedOnPublicHolidays(),
            state.minOrderAmount(),
            state.scheduledOrderEnabled(),
            state.cupDepositEnabled(),
            state.storePriceVerified()
        );
    }
}
