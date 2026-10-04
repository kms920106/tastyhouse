package com.tastyhouse.domain.product.model;

import java.util.List;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;

public final class ProductOptionSelectionRule {

    private ProductOptionSelectionRule() {
    }

    public static int minRemaining(Integer minSelect, Integer maxSelect) {
        int min = minSelect != null ? minSelect : 0;
        int max = maxSelect != null ? maxSelect : 0;
        return Math.max(Math.max(min, max), 1);
    }

    public static int minRemaining(ProductOptionGroup group) {
        return group == null ? 1 : minRemaining(group.getMinSelect(), group.getMaxSelect());
    }

    public static boolean selectable(ProductOption option) {
        return !option.isSoldOut() && option.isVisible();
    }

    public static void validateRemainingAfterBlocking(
        ProductOptionGroup group,
        ProductOption target,
        List<ProductOption> groupOptions
    ) {
        if (!selectable(target)) {
            return;
        }

        long remaining = groupOptions.stream()
            .filter(option -> !option.getId().equals(target.getId()))
            .filter(ProductOptionSelectionRule::selectable)
            .count();

        int required = minRemaining(group);
        if (remaining < required) {
            throw new DomainException(violationCodeOf(group, remaining));
        }
    }

    public static void validateZeroPriceOption(ProductOptionGroup group, List<ProductOption> options) {
        if (!group.isRequired() || options.isEmpty()) {
            return;
        }

        boolean hasZeroPrice = options.stream()
            .filter(ProductOptionSelectionRule::selectable)
            .anyMatch(option -> option.getAdditionalPrice() == null || option.getAdditionalPrice() == 0);
        if (!hasZeroPrice) {
            throw new DomainException(DomainErrorCode.PRODUCT_OPTION_GROUP_REQUIRES_ZERO_PRICE_OPTION);
        }
    }

    private static DomainErrorCode violationCodeOf(ProductOptionGroup group, long remaining) {
        int minSelect = group != null && group.getMinSelect() != null ? group.getMinSelect() : 0;
        if (remaining < Math.max(minSelect, 1)) {
            return DomainErrorCode.PRODUCT_OPTION_MIN_SELECT_VIOLATION;
        }
        return DomainErrorCode.PRODUCT_OPTION_MAX_SELECT_VIOLATION;
    }
}
