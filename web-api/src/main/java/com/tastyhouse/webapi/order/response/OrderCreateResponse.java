package com.tastyhouse.webapi.order.response;

public record OrderCreateResponse(Long id) {
    public static OrderCreateResponse from(
        Long id
    ) {
        return new OrderCreateResponse(
            id
        );
    }
}
