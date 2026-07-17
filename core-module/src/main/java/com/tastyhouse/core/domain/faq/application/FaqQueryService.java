package com.tastyhouse.core.domain.faq.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.faq.domain.model.Faq;
import com.tastyhouse.core.domain.faq.domain.model.FaqCategory;
import com.tastyhouse.core.domain.faq.domain.repository.FaqCategoryRepository;
import com.tastyhouse.core.domain.faq.domain.repository.FaqRepository;
import com.tastyhouse.core.domain.faq.domain.vo.FaqCategoryId;
import com.tastyhouse.core.domain.faq.domain.vo.FaqId;
import com.tastyhouse.core.domain.faq.application.dto.FaqSearchCondition;
import com.tastyhouse.core.domain.faq.application.dto.result.FaqCategoryManagementResult;
import com.tastyhouse.core.domain.faq.application.dto.result.FaqCategoryResult;
import com.tastyhouse.core.domain.faq.application.dto.result.FaqDetailResult;
import com.tastyhouse.core.domain.faq.application.dto.result.FaqListItemResult;
import com.tastyhouse.core.domain.faq.application.dto.result.FaqResult;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FaqQueryService {

    private final FaqRepository faqRepository;
    private final FaqCategoryRepository faqCategoryRepository;

    public List<FaqCategoryResult> findAllActiveCategories() {
        return faqCategoryRepository.findAllActiveCategories();
    }

    public List<FaqResult> findFaqItems(Long categoryId) {
        if (categoryId == null) {
            return faqRepository.findAllActiveItems();
        }
        return faqRepository.findActiveItemsByCategoryId(categoryId);
    }

    public List<FaqCategoryManagementResult> findAllCategories() {
        return faqCategoryRepository.findAllCategories();
    }

    public FaqCategoryManagementResult findCategoryDetail(FaqCategoryId faqCategoryId) {
        FaqCategory faqCategory = faqCategoryRepository.findById(faqCategoryId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.FAQ_CATEGORY_NOT_FOUND));
        return new FaqCategoryManagementResult(
            faqCategory.getId(),
            faqCategory.getName(),
            faqCategory.getSort(),
            faqCategory.isVisible(),
            faqCategory.getCreatedAt()
        );
    }

    public PageResult<FaqListItemResult> findFaqPage(FaqSearchCondition condition, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return faqRepository.findFaqPage(condition, pageQuery);
    }

    public FaqDetailResult findFaqDetail(FaqId faqId) {
        Faq faq = faqRepository.findById(faqId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.FAQ_NOT_FOUND));
        return FaqDetailResult.from(
            faq.getFaqId(),
            faq.getFaqCategoryId(),
            faq.getQuestion(),
            faq.getAnswer(),
            faq.getSort(),
            faq.isVisible(),
            faq.getCreatedAt(),
            faq.getUpdatedAt()
        );
    }
}
