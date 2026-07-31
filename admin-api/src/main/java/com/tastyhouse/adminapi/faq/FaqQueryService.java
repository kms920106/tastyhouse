package com.tastyhouse.adminapi.faq;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.faq.query.FaqCategoryManagementResult;
import com.tastyhouse.infrastructure.faq.query.FaqDetailResult;
import com.tastyhouse.infrastructure.faq.query.FaqManagementListItemResult;
import com.tastyhouse.infrastructure.faq.query.FaqQueryDao;
import com.tastyhouse.infrastructure.faq.query.FaqSearchCondition;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.faq.response.FaqCategoryResponse;
import com.tastyhouse.adminapi.faq.response.FaqDetailResponse;
import com.tastyhouse.adminapi.faq.response.FaqListItemResponse;

/**
 * FAQ 관리 조회 서비스.
 *
 * <p>infra read 어댑터({@link FaqQueryDao})만 주입해 조회하고 Response를 조립한다. write 포트를
 * 주입하지 않으며, 쓰기는 {@link FaqCommandService}·{@link FaqCategoryCommandService}가 담당한다.
 * 항목과 카테고리 조회를 한 서비스에 두는 것은 DAO가 도메인당 1개인 것과 같은 이유로, 두 애그리거트가
 * 같은 관리 화면을 구성하기 때문이다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FaqQueryService {

    private final FaqQueryDao faqQueryDao;

    public List<FaqCategoryResponse> getCategories() {
        return faqQueryDao.findAllCategories().stream()
            .map(this::toFaqCategoryResponse)
            .toList();
    }

    public FaqCategoryResponse getCategory(Long categoryId) {
        FaqCategoryManagementResult categoryDetail = faqQueryDao.findCategoryDetailById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.FAQ_CATEGORY_NOT_FOUND));
        return toFaqCategoryResponse(categoryDetail);
    }

    public PaginationResponse<FaqListItemResponse> getFaqs(Long categoryId, String question, Boolean visible, int page, int size) {
        FaqSearchCondition condition = FaqSearchCondition.of(categoryId, question, visible);
        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<FaqListItemResponse> pageResult = faqQueryDao.findAllFaqs(condition, pageQuery)
            .map(this::toFaqListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    public FaqDetailResponse getFaq(Long id) {
        FaqDetailResult faqDetail = faqQueryDao.findFaqDetailById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.FAQ_NOT_FOUND));
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
