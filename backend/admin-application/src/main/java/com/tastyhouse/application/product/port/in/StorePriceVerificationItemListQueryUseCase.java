package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.StorePriceVerificationItemResult;

public interface StorePriceVerificationItemListQueryUseCase {

    List<StorePriceVerificationItemResult> getVerificationItems(Long verificationId);
}
