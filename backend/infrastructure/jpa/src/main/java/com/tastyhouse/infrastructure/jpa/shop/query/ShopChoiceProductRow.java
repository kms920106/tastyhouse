package com.tastyhouse.infrastructure.jpa.shop.query;

import com.tastyhouse.application.product.port.out.ProductSimpleResult;

public record ShopChoiceProductRow(Long shopId, ProductSimpleResult product) {
}
