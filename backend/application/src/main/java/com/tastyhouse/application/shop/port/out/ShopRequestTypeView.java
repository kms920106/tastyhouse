package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.domain.shop.model.ShopRequestType;

public record ShopRequestTypeView(
    ShopRequestType requestType,
    boolean contractAmending
) {
}
