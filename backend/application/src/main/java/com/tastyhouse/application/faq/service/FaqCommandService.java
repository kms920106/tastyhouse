package com.tastyhouse.application.faq.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.faq.port.in.FaqCommandUseCase;
import com.tastyhouse.application.faq.port.in.FaqCreateCommand;
import com.tastyhouse.application.faq.port.in.FaqDeleteCommand;
import com.tastyhouse.application.faq.port.in.FaqUpdateCommand;
import com.tastyhouse.domain.faq.model.Faq;
import com.tastyhouse.domain.faq.repository.FaqCategoryRepository;
import com.tastyhouse.domain.faq.repository.FaqRepository;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;
import com.tastyhouse.domain.faq.vo.FaqId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

@Service
@AdminApp
@Transactional
public class FaqCommandService implements FaqCommandUseCase {

    private final FaqRepository faqRepository;
    private final FaqCategoryRepository faqCategoryRepository;

    public FaqCommandService(FaqRepository faqRepository, FaqCategoryRepository faqCategoryRepository) {
        this.faqRepository = faqRepository;
        this.faqCategoryRepository = faqCategoryRepository;
    }

    @Override
    public Long createFaq(FaqCreateCommand command) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(command.faqCategoryId());
        validateCategoryExists(faqCategoryId);

        Faq faq = Faq.of(faqCategoryId, command.question(), command.answer(), command.sort(), command.visible());
        Faq saved = faqRepository.save(faq);
        return saved.getFaqId().value();
    }

    @Override
    public void updateFaq(FaqUpdateCommand command) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(command.faqCategoryId());
        validateCategoryExists(faqCategoryId);

        FaqId faqId = FaqId.of(command.faqId());
        Faq faq = findFaqOrThrow(faqId);

        faq.update(faqCategoryId, command.question(), command.answer(), command.sort(), command.visible());
        faqRepository.save(faq);
    }

    @Override
    public void deleteFaq(FaqDeleteCommand command) {
        FaqId faqId = FaqId.of(command.faqId());
        Faq faq = findFaqOrThrow(faqId);

        faq.delete();
        faqRepository.save(faq);
    }

    private Faq findFaqOrThrow(FaqId faqId) {
        return faqRepository.findById(faqId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FAQ_NOT_FOUND));
    }

    private void validateCategoryExists(FaqCategoryId faqCategoryId) {
        faqCategoryRepository.findById(faqCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FAQ_CATEGORY_NOT_FOUND));
    }
}
