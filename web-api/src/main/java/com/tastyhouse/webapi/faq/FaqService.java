package com.tastyhouse.webapi.faq;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.faq.application.FaqQueryService;
import com.tastyhouse.core.domain.faq.application.dto.FaqCategoryResult;
import com.tastyhouse.core.domain.faq.application.dto.FaqResult;
import com.tastyhouse.webapi.faq.response.FaqCategoryListItemResponse;
import com.tastyhouse.webapi.faq.response.FaqListItemResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class FaqService {

    private final FaqQueryService faqQueryService;

    @Transactional(readOnly = true)
    public List<FaqCategoryListItemResponse> searchCategories() {
        return faqQueryService.findAllActiveCategories().stream()
                .map(this::toFaqCategoryListItemResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FaqListItemResponse> searchFaqListItemResponses(Long categoryId) {
        return faqQueryService.findFaqItems(categoryId).stream()
                .map(this::toFaqListItemResponse)
                .toList();
    }

    private FaqCategoryListItemResponse toFaqCategoryListItemResponse(FaqCategoryResult result) {
        return FaqCategoryListItemResponse.from(
            result.id(),
            result.name(),
            result.sort()
        );
    }

    private FaqListItemResponse toFaqListItemResponse(FaqResult result) {
        return FaqListItemResponse.from(
            result.id(),
            result.faqCategoryId(),
            result.question(),
            result.answer(),
            result.sort()
        );
    }
}
