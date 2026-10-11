package com.tastyhouse.application.shop.port.out;

import java.util.List;

import com.tastyhouse.application.shared.port.out.CodeLabelResult;

public record ShopRequestTypeCatalogResult(
    List<ShopRequestTypeView> requestTypes,
    List<CodeLabelResult> statuses
) {
}
