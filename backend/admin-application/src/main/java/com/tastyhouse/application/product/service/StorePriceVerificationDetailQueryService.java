package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.StorePriceVerificationDetailQueryUseCase;
import com.tastyhouse.application.product.port.out.StorePriceVerificationListItemResult;
import com.tastyhouse.application.product.port.out.StorePriceVerificationQueryPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class StorePriceVerificationDetailQueryService implements StorePriceVerificationDetailQueryUseCase {

    private final StorePriceVerificationQueryPort storePriceVerificationQueryPort;

    public StorePriceVerificationDetailQueryService(StorePriceVerificationQueryPort storePriceVerificationQueryPort) {
        this.storePriceVerificationQueryPort = storePriceVerificationQueryPort;
    }

    @Override
    public StorePriceVerificationListItemResult getVerification(Long verificationId) {
        return storePriceVerificationQueryPort.findVerificationById(verificationId)
            .orElseThrow(() -> new ResourceNotFoundException(
                ApplicationErrorCode.SHOP_STORE_PRICE_VERIFICATION_NOT_FOUND));
    }
}
