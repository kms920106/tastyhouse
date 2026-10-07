package com.tastyhouse.application.faq.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.faq.model.Faq;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;
import com.tastyhouse.application.faq.port.in.FaqCreateCommand;
import com.tastyhouse.application.faq.port.in.FaqCreateUseCase;
import com.tastyhouse.application.faq.port.out.write.FaqCategoryPersistencePort;
import com.tastyhouse.application.faq.port.out.write.FaqPersistencePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class FaqCreateService implements FaqCreateUseCase {

    private final FaqPersistencePort faqPersistencePort;
    private final FaqCategoryPersistencePort faqCategoryPersistencePort;

    public FaqCreateService(FaqPersistencePort faqPersistencePort, FaqCategoryPersistencePort faqCategoryPersistencePort) {
        this.faqPersistencePort = faqPersistencePort;
        this.faqCategoryPersistencePort = faqCategoryPersistencePort;
    }

    @Override
    public Long createFaq(FaqCreateCommand command) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(command.faqCategoryId());
        validateCategoryExists(faqCategoryId);

        Faq faq = Faq.of(faqCategoryId, command.question(), command.answer(), command.sort(), command.visible());
        Faq saved = faqPersistencePort.save(faq);
        return saved.getFaqId().value();
    }

    private void validateCategoryExists(FaqCategoryId faqCategoryId) {
        faqCategoryPersistencePort.findById(faqCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.FAQ_CATEGORY_NOT_FOUND));
    }
}
