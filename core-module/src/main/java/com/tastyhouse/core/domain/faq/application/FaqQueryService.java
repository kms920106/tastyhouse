package com.tastyhouse.core.domain.faq.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.faq.domain.repository.FaqRepository;
import com.tastyhouse.core.domain.faq.application.dto.FaqCategoryResult;
import com.tastyhouse.core.domain.faq.application.dto.FaqResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FaqQueryService {

    private final FaqRepository faqRepository;

    public List<FaqCategoryResult> findAllActiveCategories() {
        return faqRepository.findAllActiveCategories();
    }

    public List<FaqResult> findFaqItems(Long categoryId) {
        if (categoryId == null) {
            return faqRepository.findAllActiveItems();
        }
        return faqRepository.findActiveItemsByCategoryId(categoryId);
    }
}
