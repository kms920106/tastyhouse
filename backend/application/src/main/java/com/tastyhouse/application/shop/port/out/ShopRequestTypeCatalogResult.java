package com.tastyhouse.application.shop.port.out;

import java.util.List;

import com.tastyhouse.domain.shop.model.ShopRequestStatus;

public record ShopRequestTypeCatalogResult(
    List<ShopRequestTypeView> requestTypes,
    List<ShopRequestStatus> statuses
) {
}
