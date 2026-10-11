package com.tastyhouse.application.shop.port.out;

import java.time.LocalDate;

public record ShopRequestSearchCondition(
    Long shopId,
    String requestType,
    String status,
    LocalDate startDate,
    LocalDate endDate
) {

    public static ShopRequestSearchCondition of(
        Long shopId,
        String requestType,
        String status,
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
