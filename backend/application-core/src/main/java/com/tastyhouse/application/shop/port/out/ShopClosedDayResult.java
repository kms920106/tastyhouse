package com.tastyhouse.application.shop.port.out;

public record ShopClosedDayResult(
    Long id,
    String closedDayType,
    String closedDayTypeDescription
) {

    public ShopClosedDayResult withClosedDayTypeDescription(String closedDayTypeDescription) {
        return new ShopClosedDayResult(
            this.id,
            this.closedDayType,
            closedDayTypeDescription
        );
    }
}
