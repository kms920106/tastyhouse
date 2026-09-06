package com.tastyhouse.domain.product.service;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;

public final class CupDepositOptionRule {
    public static final int DEPOSIT_MIN_SELECT = 0;
    public static final int DEPOSIT_MAX_SELECT = 1;

    private CupDepositOptionRule() {
    }

    public static void validateDepositGroupSelectRange(
        ProductOptionGroupType groupType,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect
    ) {
        if (groupType == null || !groupType.isCupDeposit()) {
            return;
        }

        if (required) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_DEPOSIT_CANNOT_BE_REQUIRED);
        }
        boolean fixedRange = !multipleSelect
            && minSelect != null && minSelect == DEPOSIT_MIN_SELECT
            && maxSelect != null && maxSelect == DEPOSIT_MAX_SELECT;
        if (!fixedRange) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_DEPOSIT_SELECT_FIXED);
        }
    }

    public static void validateOptionValues(
        ProductOptionGroup group,
        Integer additionalPrice,
        Integer cupCount,
        Integer personalCupDiscountAmount,
        CupDepositPolicy cupDepositPolicy
    ) {
        boolean personalCup = personalCupDiscountAmount != null && personalCupDiscountAmount > 0;

        if (!group.isCupDeposit()) {
            if (cupCount != null) {
                throw new BusinessException(ErrorCode.PRODUCT_OPTION_CUP_COUNT_NOT_ALLOWED);
            }
            if (personalCup) {
                throw new BusinessException(ErrorCode.PRODUCT_OPTION_PERSONAL_CUP_NOT_IN_DEPOSIT_GROUP);
            }
            return;
        }

        if (additionalPrice != null && additionalPrice != 0) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_DEPOSIT_ADDITIONAL_PRICE_NOT_ALLOWED);
        }

        if (personalCup) {
            if (cupCount != null) {
                throw new BusinessException(ErrorCode.PRODUCT_OPTION_CUP_COUNT_NOT_ALLOWED);
            }
            return;
        }

        if (cupCount == null) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_CUP_COUNT_REQUIRED);
        }
        cupDepositPolicy.validateCupCount(cupCount);
    }
}
