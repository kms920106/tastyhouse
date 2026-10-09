package com.tastyhouse.infrastructure.jpa.shop.query;

public record ShopTipAggregateRow(Long shopId, Integer minAmount, Integer maxAmount) {
}
