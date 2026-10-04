package com.tastyhouse.infrastructure.persistence.shop.query;

public record ShopTipAggregateRow(Long shopId, Integer minAmount, Integer maxAmount) {
}
