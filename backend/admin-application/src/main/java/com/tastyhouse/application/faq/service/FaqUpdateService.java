package com.tastyhouse.application.faq.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.faq.model.Faq;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;
import com.tastyhouse.domain.faq.vo.FaqId;
import com.tastyhouse.application.faq.port.in.FaqUpdateCommand;
import com.tastyhouse.application.faq.port.in.FaqUpdateUseCase;
import com.tastyhouse.application.faq.port.out.write.FaqCategoryLoadPort;
import com.tastyhouse.application.faq.port.out.write.FaqLoadPort;
import com.tastyhouse.application.faq.port.out.write.FaqSavePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class FaqUpdateService implements FaqUpdateUseCase {

    private final FaqLoadPort faqLoadPort;
    private final FaqSavePort faqSavePort;
    private final FaqCategoryLoadPort faqCategoryLoadPort;

    public FaqUpdateService(FaqLoadPort faqLoadPort, FaqSavePort faqSavePort, FaqCategoryLoadPort faqCategoryLoadPort) {
        this.faqLoadPort = faqLoadPort;
        this.faqSavePort = faqSavePort;
        this.faqCategoryLoadPort = faqCategoryLoadPort;
    }

    @Override
    public void updateFaq(FaqUpdateCommand command) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(command.faqCategoryId());
        validateCategoryExists(faqCategoryId);

        FaqId faqId = FaqId.of(command.faqId());
        Faq faq = findFaqOrThrow(faqId);

        faq.update(faqCategoryId, command.question(), command.answer(), command.sort(), command.visible());
        faqSavePort.save(faq);
    }

    private Faq findFaqOrThrow(FaqId faqId) {
        return faqLoadPort.findActiveById(faqId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.FAQ_NOT_FOUND));
    }

    private void validateCategoryExists(FaqCategoryId faqCategoryId) {
        faqCategoryLoadPort.findActiveById(faqCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.FAQ_CATEGORY_NOT_FOUND));
    }
}
