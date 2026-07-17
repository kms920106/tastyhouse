package com.tastyhouse.adminapi.faq;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.faq.domain.vo.FaqCategoryId;
import com.tastyhouse.core.domain.faq.domain.vo.FaqId;
import com.tastyhouse.core.domain.faq.application.FaqCategoryCommandService;
import com.tastyhouse.core.domain.faq.application.FaqCommandService;
import com.tastyhouse.core.domain.faq.application.FaqQueryService;
import com.tastyhouse.core.domain.faq.application.dto.FaqSearchCondition;
import com.tastyhouse.core.domain.faq.application.dto.command.FaqCategoryCreateCommand;
import com.tastyhouse.core.domain.faq.application.dto.command.FaqCategoryUpdateCommand;
import com.tastyhouse.core.domain.faq.application.dto.command.FaqCreateCommand;
import com.tastyhouse.core.domain.faq.application.dto.command.FaqUpdateCommand;
import com.tastyhouse.core.domain.faq.application.dto.result.FaqCategoryManagementResult;
import com.tastyhouse.core.domain.faq.application.dto.result.FaqDetailResult;
import com.tastyhouse.core.domain.faq.application.dto.result.FaqListItemResult;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.adminapi.faq.response.FaqCategoryResponse;
import com.tastyhouse.adminapi.faq.response.FaqDetailResponse;
import com.tastyhouse.adminapi.faq.response.FaqListItemResponse;
import com.tastyhouse.adminapi.faq.response.FaqPageResponse;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqCategoryCommandService faqCategoryCommandService;
    private final FaqCommandService faqCommandService;
    private final FaqQueryService faqQueryService;

    public Long createCategory(String name, Integer sort, boolean visible) {
        FaqCategoryCreateCommand command = FaqCategoryCreateCommand.of(name, sort, visible);
        FaqCategoryId faqCategoryId = faqCategoryCommandService.createCategory(command);
        return faqCategoryId.value();
    }

    public List<FaqCategoryResponse> getCategories() {
        return faqQueryService.findAllCategories().stream()
            .map(this::toFaqCategoryResponse)
            .toList();
    }

    public FaqCategoryResponse getCategory(Long categoryId) {
        FaqCategoryManagementResult result = faqQueryService.findCategoryDetail(FaqCategoryId.of(categoryId));
        return toFaqCategoryResponse(result);
    }

    public void updateCategory(Long categoryId, String name, Integer sort, boolean visible) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(categoryId);
        FaqCategoryUpdateCommand command = FaqCategoryUpdateCommand.of(name, sort, visible);
        faqCategoryCommandService.updateCategory(faqCategoryId, command);
    }

    public void deleteCategory(Long categoryId) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(categoryId);
        faqCategoryCommandService.deleteCategory(faqCategoryId);
    }

    public Long createFaq(Long faqCategoryId, String question, String answer, Integer sort, boolean visible) {
        FaqCreateCommand command = FaqCreateCommand.of(FaqCategoryId.of(faqCategoryId), question, answer, sort, visible);
        FaqId faqId = faqCommandService.createFaq(command);
        return faqId.value();
    }

    public FaqPageResponse getFaqs(Long categoryId, String question, Boolean visible, int page, int size) {
        FaqSearchCondition condition = FaqSearchCondition.of(categoryId, question, visible);
        PageResult<FaqListItemResponse> pageResult = faqQueryService.findFaqPage(condition, page, size)
            .map(this::toFaqListItemResponse);
        return FaqPageResponse.from(pageResult);
    }

    public FaqDetailResponse getFaq(Long id) {
        FaqDetailResult faqDetail = faqQueryService.findFaqDetail(FaqId.of(id));
        return toFaqDetailResponse(faqDetail);
    }

    public void updateFaq(Long id, Long faqCategoryId, String question, String answer, Integer sort, boolean visible) {
        FaqId faqId = FaqId.of(id);
        FaqUpdateCommand command = FaqUpdateCommand.of(FaqCategoryId.of(faqCategoryId), question, answer, sort, visible);
        faqCommandService.updateFaq(faqId, command);
    }

    public void deleteFaq(Long id) {
        FaqId faqId = FaqId.of(id);
        faqCommandService.deleteFaq(faqId);
    }

    private FaqCategoryResponse toFaqCategoryResponse(FaqCategoryManagementResult result) {
        return FaqCategoryResponse.from(result.id(), result.name(), result.sort(), result.visible(), result.createdAt());
    }

    private FaqListItemResponse toFaqListItemResponse(FaqListItemResult result) {
        return FaqListItemResponse.from(
            result.id(),
            result.faqCategoryId(),
            result.question(),
            result.sort(),
            result.visible(),
            result.createdAt()
        );
    }

    private FaqDetailResponse toFaqDetailResponse(FaqDetailResult result) {
        return FaqDetailResponse.from(
            result.faqId().value(),
            result.faqCategoryId(),
            result.question(),
            result.answer(),
            result.sort(),
            result.visible(),
            result.createdAt(),
            result.updatedAt()
        );
    }
}
