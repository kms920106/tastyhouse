package com.tastyhouse.webapi.faq;

import com.tastyhouse.core.entity.faq.dto.FaqCategoryDto;
import com.tastyhouse.core.entity.faq.dto.FaqItemDto;
import com.tastyhouse.core.service.FaqCoreService;
import com.tastyhouse.webapi.faq.response.FaqCategoryItem;
import com.tastyhouse.webapi.faq.response.FaqItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FaqService {

    private final FaqCoreService faqCoreService;

    @Transactional(readOnly = true)
    public List<FaqCategoryItem> searchCategories() {
        return faqCoreService.findAllActiveCategories().stream()
                .map(this::toFaqCategoryItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FaqItem> searchFaqItems(Long categoryId) {
        return faqCoreService.findFaqItems(categoryId).stream()
                .map(this::toFaqItem)
                .toList();
    }

    private FaqCategoryItem toFaqCategoryItem(FaqCategoryDto dto) {
        return new FaqCategoryItem(dto.id(), dto.name(), dto.sort());
    }

    private FaqItem toFaqItem(FaqItemDto dto) {
        return new FaqItem(dto.id(), dto.faqCategoryId(), dto.question(), dto.answer(), dto.sort());
    }
}
