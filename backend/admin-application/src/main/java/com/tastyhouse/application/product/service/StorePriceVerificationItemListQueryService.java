package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.StorePriceVerificationItemListQueryUseCase;
import com.tastyhouse.application.product.port.out.StorePriceVerificationItemResult;
import com.tastyhouse.application.product.port.out.StorePriceVerificationQueryPort;

@Service
@Transactional(readOnly = true)
class StorePriceVerificationItemListQueryService implements StorePriceVerificationItemListQueryUseCase {

    private final StorePriceVerificationQueryPort storePriceVerificationQueryPort;

    public StorePriceVerificationItemListQueryService(StorePriceVerificationQueryPort storePriceVerificationQueryPort) {
        this.storePriceVerificationQueryPort = storePriceVerificationQueryPort;
    }

    @Override
    public List<StorePriceVerificationItemResult> getVerificationItems(Long verificationId) {
        return storePriceVerificationQueryPort.findVerificationItems(verificationId);
    }
}
