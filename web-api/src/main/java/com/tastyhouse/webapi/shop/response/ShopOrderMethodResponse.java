package com.tastyhouse.webapi.shop.response;

import java.util.List;

public record ShopOrderMethodResponse(
    List<OrderMethodItem> orderMethods
) {
    public static ShopOrderMethodResponse from(
        List<OrderMethodItem> orderMethods
    ) {
        return new ShopOrderMethodResponse(
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
