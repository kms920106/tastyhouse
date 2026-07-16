package com.tastyhouse.core.domain.faq.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.faq.domain.model.Faq;
import com.tastyhouse.core.domain.faq.domain.repository.FaqCategoryRepository;
import com.tastyhouse.core.domain.faq.domain.repository.FaqRepository;
import com.tastyhouse.core.domain.faq.domain.vo.FaqCategoryId;
import com.tastyhouse.core.domain.faq.domain.vo.FaqId;
import com.tastyhouse.core.domain.faq.application.dto.command.FaqCreateCommand;
import com.tastyhouse.core.domain.faq.application.dto.command.FaqUpdateCommand;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class FaqCommandService {

    private final FaqRepository faqRepository;
    private final FaqCategoryRepository faqCategoryRepository;

    public FaqId createFaq(FaqCreateCommand command) {
        validateCategoryExists(command.faqCategoryId());

        Faq faq = Faq.of(command.faqCategoryId().value(), command.question(), command.answer(), command.sort(), command.visible());
        Faq saved = faqRepository.save(faq);
        return saved.getFaqId();
    }

    public void updateFaq(FaqId faqId, FaqUpdateCommand command) {
        validateCategoryExists(command.faqCategoryId());

        Faq faq = faqRepository.findById(faqId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.FAQ_NOT_FOUND));

        faq.update(command.faqCategoryId().value(), command.question(), command.answer(), command.sort(), command.visible());
    }

    public void deleteFaq(FaqId faqId) {
        Faq faq = faqRepository.findById(faqId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.FAQ_NOT_FOUND));

        faq.delete();
    }

    private void validateCategoryExists(FaqCategoryId faqCategoryId) {
        faqCategoryRepository.findById(faqCategoryId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.FAQ_CATEGORY_NOT_FOUND));
    }
}
