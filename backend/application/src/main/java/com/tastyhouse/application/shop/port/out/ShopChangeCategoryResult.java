package com.tastyhouse.application.shop.port.out;

import java.util.List;

import com.tastyhouse.domain.shop.model.ShopChangeCategory;
import com.tastyhouse.domain.shop.model.ShopChangeType;

public record ShopChangeCategoryResult(
    ShopChangeCategory category,
    List<ShopChangeType> changeTypes
) {
}
