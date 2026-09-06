package com.tastyhouse.domain.product.service;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public class CupDepositPolicy {
    public static final int DEPOSIT_PER_CUP = 300;

    public static final int MAX_CUP_COUNT = 10;

    public static final int MIN_CUP_COUNT = 1;

    public int depositAmountOf(Integer cupCount) {
        if (cupCount == null || cupCount <= 0) {
            return 0;
        }
        validateCupCount(cupCount);
        return cupCount * DEPOSIT_PER_CUP;
    }

    public void validateCupCount(Integer cupCount) {
        if (cupCount == null || cupCount < MIN_CUP_COUNT || cupCount > MAX_CUP_COUNT) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_CUP_COUNT_INVALID);
        }
    }
}
