package com.tastyhouse.webapplication.faq.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.faq.port.out.FaqCategoryResult;
import com.tastyhouse.application.faq.port.out.FaqQueryPort;
import com.tastyhouse.application.faq.port.out.FaqResult;
import com.tastyhouse.webapplication.faq.port.in.FaqQueryUseCase;

/**
 * FAQ 조회 서비스.
 *
 * <p>회원 노출용 조회만 있는 도메인이라 command 서비스 없이 QueryService만 둔다. infra read
 * 어댑터({@link FaqQueryPort})를 주입해 노출(visible=true) 카테고리·항목만 조회한다.
 */
@Service
@Transactional(readOnly = true)
public class FaqQueryService implements FaqQueryUseCase {

    private final FaqQueryPort faqQueryPort;

    public FaqQueryService(FaqQueryPort faqQueryPort) {
        this.faqQueryPort = faqQueryPort;
    }

    @Override
    public List<FaqCategoryResult> getFaqCategories() {
        return faqQueryPort.findVisibleCategories();
    }

    /**
     * 노출 FAQ 목록을 조회한다. categoryId가 null이면 전체 카테고리를 대상으로 한다.
     */
    @Override
    public List<FaqResult> getFaqList(Long categoryId) {
        return faqQueryPort.findVisibleFaqs(categoryId);
    }
}
