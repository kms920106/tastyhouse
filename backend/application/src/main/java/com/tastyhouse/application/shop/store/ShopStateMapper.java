package com.tastyhouse.application.shop.store;

import com.tastyhouse.application.shop.port.out.write.ShopState;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.vo.StationId;

final class ShopStateMapper {
    private ShopStateMapper() {
    }

    static Shop toDomain(ShopState state) {
        return Shop.reconstitute(
            state.id(),
            state.ceoId() == null ? null : CeoId.of(state.ceoId()),
            state.stationId() == null ? null : StationId.of(state.stationId()),
            state.name(),
            state.latitude(),
            state.longitude(),
            state.rating(),
            state.roadAddress(),
            state.lotAddress(),
            state.phoneNumber(),
            state.thumbnailImageFileId() == null ? null : UploadedFileId.of(state.thumbnailImageFileId()),
            state.trademarkImageFileId() == null ? null : UploadedFileId.of(state.trademarkImageFileId()),
            state.permanentlyClosed(),
            state.hidden(),
            state.closedOnPublicHolidays(),
            state.minOrderAmount(),
            state.scheduledOrderEnabled(),
            state.cupDepositEnabled(),
            state.storePriceVerified(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ShopState toState(Shop shop) {
        return new ShopState(
            shop.getId(),
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
            shop.isStorePriceVerified(),
            shop.getCreatedAt(),
            shop.getUpdatedAt()
        );
    }
}
