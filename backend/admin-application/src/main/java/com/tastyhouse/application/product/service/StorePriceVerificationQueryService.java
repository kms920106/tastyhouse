package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.application.product.port.in.StorePriceVerificationQueryUseCase;
import com.tastyhouse.application.product.port.out.StorePriceVerificationItemResult;
import com.tastyhouse.application.product.port.out.StorePriceVerificationListItemResult;
import com.tastyhouse.application.product.port.out.StorePriceVerificationQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class StorePriceVerificationQueryService implements StorePriceVerificationQueryUseCase {

    private final StorePriceVerificationQueryPort storePriceVerificationQueryPort;

    public StorePriceVerificationQueryService(StorePriceVerificationQueryPort storePriceVerificationQueryPort) {
        this.storePriceVerificationQueryPort = storePriceVerificationQueryPort;
    }

    @Override
    public PageResult<StorePriceVerificationListItemResult> getVerifications(
        String status,
        int page,
        int size
    ) {
        String verificationStatus = demoteStatus(status);

        return storePriceVerificationQueryPort.findVerificationPage(verificationStatus, PageQuery.of(page, size));
    }

    @Override
    public StorePriceVerificationListItemResult getVerification(Long verificationId) {
        return storePriceVerificationQueryPort.findVerificationById(verificationId)
            .orElseThrow(() -> new ResourceNotFoundException(
                ErrorCode.SHOP_STORE_PRICE_VERIFICATION_NOT_FOUND));
    }

    @Override
    public List<StorePriceVerificationItemResult> getVerificationItems(Long verificationId) {
        return storePriceVerificationQueryPort.findVerificationItems(verificationId);
    }

    private String demoteStatus(String status) {
        return status == null ? null : StorePriceVerificationStatus.from(status).name();
    }
}
