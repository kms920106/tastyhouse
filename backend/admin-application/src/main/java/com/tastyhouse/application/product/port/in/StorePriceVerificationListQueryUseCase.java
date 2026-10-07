package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.StorePriceVerificationListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface StorePriceVerificationListQueryUseCase {

    PageResult<StorePriceVerificationListItemResult> getVerifications(
        String status,
        int page,
        int size
    );
}
