package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.List;

import com.tastyhouse.application.product.port.out.StorePriceVerificationItemResult;
import com.tastyhouse.application.product.port.out.StorePriceVerificationListItemResult;
import com.tastyhouse.domain.shared.page.PageResult;

@AdminApp
public interface StorePriceVerificationQueryUseCase {

    PageResult<StorePriceVerificationListItemResult> getVerifications(
        String status,
        int page,
        int size
    );

    StorePriceVerificationListItemResult getVerification(Long verificationId);

    List<StorePriceVerificationItemResult> getVerificationItems(Long verificationId);
}
