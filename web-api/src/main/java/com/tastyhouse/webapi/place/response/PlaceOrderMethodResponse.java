package com.tastyhouse.webapi.place.response;

import java.util.List;

public record PlaceOrderMethodResponse(
    Long placeId,
    List<OrderMethodItem> orderMethods
) {
    public static PlaceOrderMethodResponse from(
    Long placeId,
    List<OrderMethodItem> orderMethods
    ) {
    return new PlaceOrderMethodResponse(
        placeId,
        orderMethods
    );
    }

    public record OrderMethodItem(
    String code,
    String name
    ) {
    public static OrderMethodItem from(
        String code,
        String name
    ) {
        return new OrderMethodItem(
            code,
            name
        );
    }
    }
}
