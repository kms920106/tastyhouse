package com.tastyhouse.webapi.order.response;

public record OrderItemOptionResponse(
    Long id,
    String optionGroupName,
    String optionName,
    Integer additionalPrice
) {
    public static OrderItemOptionResponse from(
        Long id,
        String optionGroupName,
        String optionName,
        Integer additionalPrice
    ) {
        return new OrderItemOptionResponse(
            id,
            optionGroupName,
            optionName,
            additionalPrice
        );
    }
}
