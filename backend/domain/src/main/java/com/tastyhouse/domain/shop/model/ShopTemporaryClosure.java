package com.tastyhouse.domain.shop.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopTemporaryClosure {
    private final Long id;
    private final ShopId shopId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalDateTime createdAt;

    private ShopTemporaryClosure(Long id, ShopId shopId, LocalDate startDate, LocalDate endDate, LocalDateTime createdAt) {
        this.id = id;
        this.shopId = shopId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
    }

    public static ShopTemporaryClosure of(ShopId shopId, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.SHOP_TEMPORARY_CLOSURE_INVALID_PERIOD);
        }

        return new ShopTemporaryClosure(null, shopId, startDate, endDate, null);
    }

    public static ShopTemporaryClosure reconstitute(Long id, ShopId shopId, LocalDate startDate, LocalDate endDate, LocalDateTime createdAt) {
        return new ShopTemporaryClosure(id, shopId, startDate, endDate, createdAt);
    }

    public long days() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
