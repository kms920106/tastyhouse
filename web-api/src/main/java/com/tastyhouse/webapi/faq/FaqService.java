package com.tastyhouse.webapi.faq;

import com.tastyhouse.core.entity.faq.dto.FaqCategoryDto;
import com.tastyhouse.core.entity.faq.dto.FaqItemDto;
import com.tastyhouse.core.service.FaqCoreService;
import com.tastyhouse.webapi.faq.response.FaqCategoryListItemResponse;
import com.tastyhouse.webapi.faq.response.FaqListItemResponse;
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
    public List<FaqCategoryListItemResponse> searchCategories() {
        return faqCoreService.findAllActiveCategories().stream()
                .map(this::toFaqCategoryListItemResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FaqListItemResponse> searchFaqListItemResponses(Long categoryId) {
        return faqCoreService.findFaqItems(categoryId).stream()
                .map(this::toFaqListItemResponse)
                .toList();
    }

    private FaqCategoryListItemResponse toFaqCategoryListItemResponse(FaqCategoryDto dto) {
        return FaqCategoryListItemResponse.from(
            dto.id(),
            dto.name(),
            dto.sort()
        );
    }

    private FaqListItemResponse toFaqListItemResponse(FaqItemDto dto) {
        return FaqListItemResponse.from(
            dto.id(),
            dto.faqCategoryId(),
            dto.question(),
            dto.answer(),
            dto.sort()
        );
    }
}
