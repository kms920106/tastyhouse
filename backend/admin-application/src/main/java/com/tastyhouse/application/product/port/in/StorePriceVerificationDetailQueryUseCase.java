package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.StorePriceVerificationListItemResult;

public interface StorePriceVerificationDetailQueryUseCase {

    StorePriceVerificationListItemResult getVerification(Long verificationId);
}
