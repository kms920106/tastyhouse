package com.tastyhouse.infrastructure.shop.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP")
public class ShopJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ceo_id")
    private Long ceoId;

    @Column(name = "station_id", nullable = false)
    private Long stationId;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "latitude", nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false)
    private BigDecimal longitude;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "road_address")
    private String roadAddress;

    @Column(name = "lot_address")
    private String lotAddress;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "thumbnail_image_file_id")
    private Long thumbnailImageFileId;

    @Column(name = "trademark_image_file_id")
    private Long trademarkImageFileId;

    @Column(name = "is_permanently_closed", nullable = false)
    private boolean permanentlyClosed;

    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;

    @Column(name = "is_closed_on_public_holidays", nullable = false)
    private boolean closedOnPublicHolidays;

    @Column(name = "min_order_amount", nullable = false)
    private int minOrderAmount;

    @Column(name = "scheduled_order_enabled", nullable = false)
    private boolean scheduledOrderEnabled;

    @Column(name = "cup_deposit_enabled", nullable = false)
    private boolean cupDepositEnabled;

    @Column(name = "is_store_price_verified", nullable = false)
    private boolean storePriceVerified;

    protected ShopJpaEntity() {
    }

    private ShopJpaEntity(
        Long ceoId,
        Long stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Double rating,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        Long thumbnailImageFileId,
        Long trademarkImageFileId,
        boolean permanentlyClosed,
        boolean hidden,
        boolean closedOnPublicHolidays,
        int minOrderAmount,
        boolean scheduledOrderEnabled,
        boolean cupDepositEnabled,
        boolean storePriceVerified
    ) {
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
    }

    static ShopJpaEntity create(
        Long ceoId,
        Long stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Double rating,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        Long thumbnailImageFileId,
        Long trademarkImageFileId,
        boolean permanentlyClosed,
        boolean hidden,
        boolean closedOnPublicHolidays,
        int minOrderAmount,
        boolean scheduledOrderEnabled,
        boolean cupDepositEnabled,
        boolean storePriceVerified
    ) {
        return new ShopJpaEntity(
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
            storePriceVerified
        );
    }

    void applyChanges(
        Long ceoId,
        Long stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Double rating,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        Long thumbnailImageFileId,
        Long trademarkImageFileId,
        boolean permanentlyClosed,
        boolean hidden,
        boolean closedOnPublicHolidays,
        int minOrderAmount,
        boolean scheduledOrderEnabled,
        boolean cupDepositEnabled,
        boolean storePriceVerified
    ) {
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
    }

    public Long getId() {
        return this.id;
    }

    public boolean isStorePriceVerified() {
        return this.storePriceVerified;
    }

    public Long getCeoId() {
        return this.ceoId;
    }

    public Long getStationId() {
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

    public Long getThumbnailImageFileId() {
        return this.thumbnailImageFileId;
    }

    public Long getTrademarkImageFileId() {
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

    public int getMinOrderAmount() {
        return this.minOrderAmount;
    }

    public boolean isScheduledOrderEnabled() {
        return this.scheduledOrderEnabled;
    }

    public boolean isCupDepositEnabled() {
        return this.cupDepositEnabled;
    }
}
