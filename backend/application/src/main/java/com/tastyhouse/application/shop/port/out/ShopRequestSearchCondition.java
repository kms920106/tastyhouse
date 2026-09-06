package com.tastyhouse.application.shop.port.out;

import java.time.LocalDate;

import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;

public record ShopRequestSearchCondition(
    Long shopId,
    ShopRequestType requestType,
    ShopRequestStatus status,
    LocalDate startDate,
    LocalDate endDate
) {

    public static ShopRequestSearchCondition of(
        Long shopId,
        ShopRequestType requestType,
        ShopRequestStatus status,
        LocalDate startDate,
        LocalDate endDate
    ) {
        return new ShopRequestSearchCondition(
            shopId,
            requestType,
            status,
            startDate,
            endDate
        );
    }
}
