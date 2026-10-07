package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.application.product.port.in.StorePriceVerificationListQueryUseCase;
import com.tastyhouse.application.product.port.out.StorePriceVerificationListItemResult;
import com.tastyhouse.application.product.port.out.StorePriceVerificationQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class StorePriceVerificationListQueryService implements StorePriceVerificationListQueryUseCase {

    private final StorePriceVerificationQueryPort storePriceVerificationQueryPort;

    public StorePriceVerificationListQueryService(StorePriceVerificationQueryPort storePriceVerificationQueryPort) {
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

    private String demoteStatus(String status) {
        return status == null ? null : StorePriceVerificationStatus.from(status).name();
    }
}
