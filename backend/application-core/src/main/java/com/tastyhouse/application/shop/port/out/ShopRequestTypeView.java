package com.tastyhouse.application.shop.port.out;

public record ShopRequestTypeView(
    String requestType,
    String requestTypeDescription,
    boolean contractAmending
) {
}
