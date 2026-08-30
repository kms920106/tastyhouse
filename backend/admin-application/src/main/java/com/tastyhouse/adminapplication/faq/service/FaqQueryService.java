package com.tastyhouse.adminapplication.faq.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.faq.port.out.FaqCategoryManagementResult;
import com.tastyhouse.application.faq.port.out.FaqDetailResult;
import com.tastyhouse.application.faq.port.out.FaqManagementListItemResult;
import com.tastyhouse.application.faq.port.out.FaqQueryPort;
import com.tastyhouse.application.faq.port.out.FaqSearchCondition;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapplication.faq.response.FaqCategoryResponse;
import com.tastyhouse.adminapplication.faq.response.FaqDetailResponse;
import com.tastyhouse.adminapplication.faq.response.FaqListItemResponse;
import com.tastyhouse.adminapplication.faq.port.in.FaqQueryUseCase;

/**
 * FAQ 관리 조회 서비스.
 *
 * <p>읽기 포트({@link FaqQueryPort})만 주입해 조회하고 Response를 조립한다. write 포트를
 * 주입하지 않으며, 쓰기는 {@link FaqCommandService}·{@link FaqCategoryCommandService}가 담당한다.
 * 항목과 카테고리 조회를 한 서비스에 두는 것은 DAO가 도메인당 1개인 것과 같은 이유로, 두 애그리거트가
 * 같은 관리 화면을 구성하기 때문이다.
 */
@Service
@Transactional(readOnly = true)
public class FaqQueryService implements FaqQueryUseCase {

    private final FaqQueryPort faqQueryPort;

    public FaqQueryService(FaqQueryPort faqQueryPort) {
        this.faqQueryPort = faqQueryPort;
    }

    @Override
    public List<FaqCategoryResponse> getCategories() {
        return faqQueryPort.findAllCategories().stream()
            .map(this::toFaqCategoryResponse)
            .toList();
    }

    @Override
    public FaqCategoryResponse getCategory(Long categoryId) {
        FaqCategoryManagementResult categoryDetail = faqQueryPort.findCategoryDetailById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FAQ_CATEGORY_NOT_FOUND));
        return toFaqCategoryResponse(categoryDetail);
    }

    @Override
    public PaginationResponse<FaqListItemResponse> getFaqs(Long categoryId, String question, Boolean visible, int page, int size) {
        FaqSearchCondition condition = FaqSearchCondition.of(categoryId, question, visible);
        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<FaqListItemResponse> pageResult = faqQueryPort.findAllFaqs(condition, pageQuery)
            .map(this::toFaqListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    @Override
    public FaqDetailResponse getFaq(Long id) {
        FaqDetailResult faqDetail = faqQueryPort.findFaqDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FAQ_NOT_FOUND));
        return toFaqDetailResponse(faqDetail);
    }

    private FaqCategoryResponse toFaqCategoryResponse(FaqCategoryManagementResult dto) {
        return FaqCategoryResponse.from(dto.id(), dto.name(), dto.sort(), dto.visible(), dto.createdAt());
    }

    private FaqListItemResponse toFaqListItemResponse(FaqManagementListItemResult dto) {
        return FaqListItemResponse.from(
            dto.id(),
            dto.faqCategoryId(),
            dto.question(),
            dto.sort(),
            dto.visible(),
            dto.createdAt()
        );
    }

    private FaqDetailResponse toFaqDetailResponse(FaqDetailResult dto) {
        return FaqDetailResponse.from(
            dto.id(),
            dto.faqCategoryId(),
            dto.question(),
            dto.answer(),
            dto.sort(),
            dto.visible(),
            dto.createdAt(),
            dto.updatedAt()
        );
    }
}
