package com.tastyhouse.webapi.faq;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.infrastructure.faq.query.FaqCategoryResult;
import com.tastyhouse.infrastructure.faq.query.FaqQueryDao;
import com.tastyhouse.infrastructure.faq.query.FaqResult;
import com.tastyhouse.webapi.faq.response.FaqCategoryListItemResponse;
import com.tastyhouse.webapi.faq.response.FaqListItemResponse;

/**
 * FAQ 조회 서비스.
 *
 * <p>회원 노출용 조회만 있는 도메인이라 command 서비스 없이 QueryService만 둔다. infra read
 * 어댑터({@link FaqQueryDao})를 주입해 노출(visible=true) 카테고리·항목만 조회한다.
 */
@Service
@Transactional(readOnly = true)
public class FaqQueryService {

    private final FaqQueryDao faqQueryDao;

    public FaqQueryService(FaqQueryDao faqQueryDao) {
        this.faqQueryDao = faqQueryDao;
    }

    public List<FaqCategoryListItemResponse> getFaqCategories() {
        return faqQueryDao.findVisibleCategories().stream()
            .map(this::toFaqCategoryListItemResponse)
            .toList();
    }

    /**
     * 노출 FAQ 목록을 조회한다. categoryId가 null이면 전체 카테고리를 대상으로 한다.
     */
    public List<FaqListItemResponse> getFaqList(Long categoryId) {
        return faqQueryDao.findVisibleFaqs(categoryId).stream()
            .map(this::toFaqListItemResponse)
            .toList();
    }

    private FaqCategoryListItemResponse toFaqCategoryListItemResponse(FaqCategoryResult dto) {
        return FaqCategoryListItemResponse.from(
            dto.id(),
            dto.name(),
            dto.sort()
        );
    }

    private FaqListItemResponse toFaqListItemResponse(FaqResult dto) {
        return FaqListItemResponse.from(
            dto.id(),
            dto.faqCategoryId(),
            dto.question(),
            dto.answer(),
            dto.sort()
        );
    }
}
