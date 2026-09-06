package com.tastyhouse.application.shop.port.out;

import java.time.LocalDate;

import com.tastyhouse.domain.shop.model.ShopCeoAssignmentActionType;

public record ShopCeoAssignmentHistorySearchCondition(
    Long ceoId,
    Long shopId,
    ShopCeoAssignmentActionType actionType,
    LocalDate startDate,
    LocalDate endDate
) {

    public static ShopCeoAssignmentHistorySearchCondition of(
        Long ceoId,
        Long shopId,
        ShopCeoAssignmentActionType actionType,
        LocalDate startDate,
        LocalDate endDate
    ) {
        return new ShopCeoAssignmentHistorySearchCondition(
            ceoId,
            shopId,
            actionType,
            startDate,
            endDate
        );
    }
}
