package com.tastyhouse.application.faq.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.faq.model.Faq;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;
import com.tastyhouse.domain.faq.vo.FaqId;
import com.tastyhouse.application.faq.port.in.FaqUpdateCommand;
import com.tastyhouse.application.faq.port.in.FaqUpdateUseCase;
import com.tastyhouse.application.faq.port.out.write.FaqCategoryPersistencePort;
import com.tastyhouse.application.faq.port.out.write.FaqPersistencePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class FaqUpdateService implements FaqUpdateUseCase {

    private final FaqPersistencePort faqPersistencePort;
    private final FaqCategoryPersistencePort faqCategoryPersistencePort;

    public FaqUpdateService(FaqPersistencePort faqPersistencePort, FaqCategoryPersistencePort faqCategoryPersistencePort) {
        this.faqPersistencePort = faqPersistencePort;
        this.faqCategoryPersistencePort = faqCategoryPersistencePort;
    }

    @Override
    public void updateFaq(FaqUpdateCommand command) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(command.faqCategoryId());
        validateCategoryExists(faqCategoryId);

        FaqId faqId = FaqId.of(command.faqId());
        Faq faq = findFaqOrThrow(faqId);

        faq.update(faqCategoryId, command.question(), command.answer(), command.sort(), command.visible());
        faqPersistencePort.save(faq);
    }

    private Faq findFaqOrThrow(FaqId faqId) {
        return faqPersistencePort.findById(faqId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.FAQ_NOT_FOUND));
    }

    private void validateCategoryExists(FaqCategoryId faqCategoryId) {
        faqCategoryPersistencePort.findById(faqCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.FAQ_CATEGORY_NOT_FOUND));
    }
}
