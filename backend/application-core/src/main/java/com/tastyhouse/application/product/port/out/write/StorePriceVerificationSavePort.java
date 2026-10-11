package com.tastyhouse.application.product.port.out.write;

import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.product.model.StorePriceVerificationItem;

public interface StorePriceVerificationSavePort {

    StorePriceVerification save(StorePriceVerification verification);

    void saveItem(StorePriceVerificationItem item);
}
