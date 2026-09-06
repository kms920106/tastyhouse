package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.product.port.out.StorePriceVerificationItemResult;
import com.tastyhouse.application.product.port.out.StorePriceVerificationListItemResult;
import com.tastyhouse.application.product.port.out.StorePriceVerificationQueryPort;
import com.tastyhouse.application.product.port.in.StorePriceVerificationQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class StorePriceVerificationQueryService implements StorePriceVerificationQueryUseCase {

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
        StorePriceVerificationStatus verificationStatus = promoteStatus(status);

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

    private StorePriceVerificationStatus promoteStatus(String status) {
        return status == null ? null : StorePriceVerificationStatus.from(status);
    }
}
