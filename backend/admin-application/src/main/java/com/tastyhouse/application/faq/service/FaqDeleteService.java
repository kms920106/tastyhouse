package com.tastyhouse.application.faq.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.faq.model.Faq;
import com.tastyhouse.domain.faq.vo.FaqId;
import com.tastyhouse.application.faq.port.in.FaqDeleteCommand;
import com.tastyhouse.application.faq.port.in.FaqDeleteUseCase;
import com.tastyhouse.application.faq.port.out.write.FaqPersistencePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class FaqDeleteService implements FaqDeleteUseCase {

    private final FaqPersistencePort faqPersistencePort;

    public FaqDeleteService(FaqPersistencePort faqPersistencePort) {
        this.faqPersistencePort = faqPersistencePort;
    }

    @Override
    public void deleteFaq(FaqDeleteCommand command) {
        FaqId faqId = FaqId.of(command.faqId());
        Faq faq = findFaqOrThrow(faqId);

        faq.delete();
        faqPersistencePort.save(faq);
    }

    private Faq findFaqOrThrow(FaqId faqId) {
        return faqPersistencePort.findById(faqId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.FAQ_NOT_FOUND));
    }
}
