package com.tastyhouse.application.product.service;

import java.util.List;

import com.tastyhouse.domain.product.model.CupDepositPolicy;
import com.tastyhouse.application.product.port.out.BatchOptionResult;
import com.tastyhouse.application.product.port.out.OptionGroupResult;
import com.tastyhouse.application.product.port.out.OptionResult;
import com.tastyhouse.application.product.port.out.ProductBatchResult;
import com.tastyhouse.application.product.port.out.ProductOptionsResult;

final class ProductOptionDepositAmounts {

    private ProductOptionDepositAmounts() {
    }

    static ProductOptionsResult of(ProductOptionsResult result, CupDepositPolicy cupDepositPolicy) {
        return new ProductOptionsResult(result.optionGroups().stream()
            .map(group -> new OptionGroupResult(
                group.id(),
                group.name(),
                group.description(),
                group.required(),
                group.multipleSelect(),
                group.minSelect(),
                group.maxSelect(),
                group.common(),
                group.groupType(),
                group.options().stream()
                    .map(option -> new OptionResult(
                        option.id(),
                        option.name(),
                        option.additionalPrice(),
                        option.soldOut(),
                        option.cupCount(),
                        option.depositAmount() != null
                            ? option.depositAmount()
                            : cupDepositPolicy.depositAmountOf(option.cupCount()),
                        option.personalCupDiscountAmount()
                    ))
                    .toList()
            ))
            .toList());
    }

    static List<ProductBatchResult> of(List<ProductBatchResult> results, CupDepositPolicy cupDepositPolicy) {
        return results.stream()
            .map(result -> new ProductBatchResult(
                result.id(),
                result.available(),
                result.name(),
                result.imageUrl(),
                result.originalPrice(),
                result.discountPrice(),
                result.discountRate(),
                result.options().stream()
                    .map(option -> new BatchOptionResult(
                        option.id(),
                        option.name(),
                        option.price(),
                        option.cupCount(),
                        cupDepositPolicy.depositAmountOf(option.cupCount()),
                        option.personalCupDiscountAmount()
                    ))
                    .toList()
            ))
            .toList();
    }
}
