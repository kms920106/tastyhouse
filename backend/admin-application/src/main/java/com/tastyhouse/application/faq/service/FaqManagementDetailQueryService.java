package com.tastyhouse.application.faq.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.faq.port.in.FaqManagementDetailQueryUseCase;
import com.tastyhouse.application.faq.port.out.FaqDetailResult;
import com.tastyhouse.application.faq.port.out.FaqManagementQueryPort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class FaqManagementDetailQueryService implements FaqManagementDetailQueryUseCase {

    private final FaqManagementQueryPort faqManagementQueryPort;

    public FaqManagementDetailQueryService(FaqManagementQueryPort faqManagementQueryPort) {
        this.faqManagementQueryPort = faqManagementQueryPort;
    }

    @Override
    public FaqDetailResult getFaq(Long id) {
        return faqManagementQueryPort.findFaqDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.FAQ_NOT_FOUND));
    }
}
