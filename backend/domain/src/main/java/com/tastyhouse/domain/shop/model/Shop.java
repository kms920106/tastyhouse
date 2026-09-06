package com.tastyhouse.domain.shop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.StationId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public class Shop {
    public static final int MIN_ORDER_AMOUNT_UNSET = 0;

    public static final int MIN_ORDER_AMOUNT_LOWER_BOUND = 5000;

    public static final int MIN_ORDER_AMOUNT_UPPER_BOUND = 30000;

    private final Long id;
    private CeoId ceoId;
    private StationId stationId;
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private final Double rating;
    private String roadAddress;
    private String lotAddress;
    private String phoneNumber;
    private UploadedFileId thumbnailImageFileId;
    private UploadedFileId trademarkImageFileId;
    private boolean permanentlyClosed;
    private boolean hidden;
    private boolean closedOnPublicHolidays;
    private int minOrderAmount;
    private boolean scheduledOrderEnabled;

    private boolean cupDepositEnabled;

    private boolean storePriceVerified;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Shop(
        Long id,
        CeoId ceoId,
        StationId stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Double rating,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        UploadedFileId thumbnailImageFileId,
        UploadedFileId trademarkImageFileId,
        boolean permanentlyClosed,
        boolean hidden,
        boolean closedOnPublicHolidays,
        int minOrderAmount,
        boolean scheduledOrderEnabled,
        boolean cupDepositEnabled,
        boolean storePriceVerified,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.ceoId = ceoId;
        this.stationId = stationId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.roadAddress = roadAddress;
        this.lotAddress = lotAddress;
        this.phoneNumber = phoneNumber;
        this.thumbnailImageFileId = thumbnailImageFileId;
        this.trademarkImageFileId = trademarkImageFileId;
        this.permanentlyClosed = permanentlyClosed;
        this.hidden = hidden;
        this.closedOnPublicHolidays = closedOnPublicHolidays;
        this.minOrderAmount = minOrderAmount;
        this.scheduledOrderEnabled = scheduledOrderEnabled;
        this.cupDepositEnabled = cupDepositEnabled;
        this.storePriceVerified = storePriceVerified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Shop of(
        StationId stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        UploadedFileId thumbnailImageFileId
    ) {
        return new Shop(
            null,
            null,
            stationId,
            name,
            latitude,
            longitude,
            null,
            roadAddress,
            lotAddress,
            phoneNumber,
            thumbnailImageFileId,
            null,
            false,
            false,
            false,
            MIN_ORDER_AMOUNT_UNSET,
            false,
            false,
            false,
            null,
            null
        );
    }

    public static Shop reconstitute(
        Long id,
        CeoId ceoId,
        StationId stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Double rating,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        UploadedFileId thumbnailImageFileId,
        UploadedFileId trademarkImageFileId,
        boolean permanentlyClosed,
        boolean hidden,
        boolean closedOnPublicHolidays,
        int minOrderAmount,
        boolean scheduledOrderEnabled,
        boolean cupDepositEnabled,
        boolean storePriceVerified,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Shop(
            id,
            ceoId,
            stationId,
            name,
            latitude,
            longitude,
            rating,
            roadAddress,
            lotAddress,
            phoneNumber,
            thumbnailImageFileId,
            trademarkImageFileId,
            permanentlyClosed,
            hidden,
            closedOnPublicHolidays,
            minOrderAmount,
            scheduledOrderEnabled,
            cupDepositEnabled,
            storePriceVerified,
            createdAt,
            updatedAt
        );
    }

    public ShopId getShopId() {
        return ShopId.of(this.id);
    }

    public void update(
        StationId stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        UploadedFileId thumbnailImageFileId
    ) {
        validateNotPermanentlyClosed();

        this.stationId = stationId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.roadAddress = roadAddress;
        this.lotAddress = lotAddress;
        this.phoneNumber = phoneNumber;
        this.thumbnailImageFileId = thumbnailImageFileId;
    }

    public void assignCeo(CeoId ceoId) {
        this.ceoId = ceoId;
    }

    public void changePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void changeTrademarkImage(UploadedFileId trademarkImageFileId) {
        this.trademarkImageFileId = trademarkImageFileId;
    }

    public void changeThumbnailImage(UploadedFileId thumbnailImageFileId) {
        this.thumbnailImageFileId = thumbnailImageFileId;
    }

    public void updateHolidayClosure(boolean closedOnPublicHolidays) {
        this.closedOnPublicHolidays = closedOnPublicHolidays;
    }

    public void changeMinOrderAmount(int minOrderAmount) {
        validateNotPermanentlyClosed();

        if (minOrderAmount != MIN_ORDER_AMOUNT_UNSET
            && (minOrderAmount < MIN_ORDER_AMOUNT_LOWER_BOUND || minOrderAmount > MIN_ORDER_AMOUNT_UPPER_BOUND)) {
            throw new BusinessException(ErrorCode.SHOP_MIN_ORDER_AMOUNT_OUT_OF_RANGE);
        }

        this.minOrderAmount = minOrderAmount;
    }

    public void validateMinOrderAmount(OrderMethod orderMethod, int orderAmountAfterProductDiscount) {
        if (minOrderAmount == MIN_ORDER_AMOUNT_UNSET || orderMethod != OrderMethod.DELIVERY) {
            return;
        }

        if (orderAmountAfterProductDiscount < minOrderAmount) {
            throw new BusinessException(ErrorCode.SHOP_MINIMUM_ORDER_AMOUNT_NOT_MET);
        }
    }

    public void changeScheduledOrderEnabled(boolean scheduledOrderEnabled) {
        validateNotPermanentlyClosed();

        this.scheduledOrderEnabled = scheduledOrderEnabled;
    }

    public void changeCupDepositEnabled(boolean cupDepositEnabled) {
        validateNotPermanentlyClosed();

        this.cupDepositEnabled = cupDepositEnabled;
    }

    public void verifyStorePrice() {
        this.storePriceVerified = true;
    }

    public void clearStorePriceVerification() {
        this.storePriceVerified = false;
    }

    public boolean isStorePriceVerified() {
        return this.storePriceVerified;
    }

    public boolean canUseCupDeposit() {
        return this.cupDepositEnabled;
    }

    public void validateCupDepositEnabled() {
        if (!this.cupDepositEnabled) {
            throw new BusinessException(ErrorCode.SHOP_CUP_DEPOSIT_NOT_ENABLED);
        }
    }

    public void hide() {
        this.hidden = true;
    }

    public void show() {
        validateNotPermanentlyClosed();

        this.hidden = false;
    }

    public void close() {
        this.permanentlyClosed = true;
    }

    private void validateNotPermanentlyClosed() {
        if (permanentlyClosed) {
            throw new BusinessException(ErrorCode.SHOP_ALREADY_PERMANENTLY_CLOSED);
        }
    }

    public Long getId() {
        return this.id;
    }

    public CeoId getCeoId() {
        return this.ceoId;
    }

    public StationId getStationId() {
        return this.stationId;
    }

    public String getName() {
        return this.name;
    }

    public BigDecimal getLatitude() {
        return this.latitude;
    }

    public BigDecimal getLongitude() {
        return this.longitude;
    }

    public Double getRating() {
        return this.rating;
    }

    public String getRoadAddress() {
        return this.roadAddress;
    }

    public String getLotAddress() {
        return this.lotAddress;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public UploadedFileId getThumbnailImageFileId() {
        return this.thumbnailImageFileId;
    }

    public UploadedFileId getTrademarkImageFileId() {
        return this.trademarkImageFileId;
    }

    public boolean isPermanentlyClosed() {
        return this.permanentlyClosed;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean isClosedOnPublicHolidays() {
        return this.closedOnPublicHolidays;
    }

    public boolean isCupDepositEnabled() {
        return this.cupDepositEnabled;
    }

    public int getMinOrderAmount() {
        return this.minOrderAmount;
    }

    public boolean isScheduledOrderEnabled() {
        return this.scheduledOrderEnabled;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
