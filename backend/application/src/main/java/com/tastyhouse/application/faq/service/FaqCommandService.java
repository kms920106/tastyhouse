package com.tastyhouse.application.faq.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.faq.port.in.FaqCommandUseCase;
import com.tastyhouse.application.faq.port.in.FaqCreateCommand;
import com.tastyhouse.application.faq.port.in.FaqDeleteCommand;
import com.tastyhouse.application.faq.port.in.FaqUpdateCommand;
import com.tastyhouse.domain.faq.model.Faq;
import com.tastyhouse.domain.faq.repository.FaqCategoryRepository;
import com.tastyhouse.domain.faq.repository.FaqRepository;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;
import com.tastyhouse.domain.faq.vo.FaqId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * FAQ 항목 관리 command 서비스.
 *
 * <p>domain write 포트({@link FaqRepository}·{@link FaqCategoryRepository})만 주입해 생성·수정·삭제를
 * 수행한다. 조회는 {@link FaqManagementQueryService}가 담당하며, 이 서비스는 infra query DAO를 주입하지 않는다.
 * {@link FaqCategoryRepository}는 소속 카테고리 존재 검증(단건 로드)에만 쓰이므로 write 포트 주입이
 * 맞다 — 두 애그리거트를 함께 save하지 않으므로 도메인 서비스로 하강시키지 않았다.
 *
 * <p>{@code Faq}는 순수 POJO라 더티 체킹이 없으므로 도메인 변경 후 명시적으로
 * {@code faqRepository.save(faq)}를 호출한다.
 */
@Service
@AdminApp
@Transactional
public class FaqCommandService implements FaqCommandUseCase {

    private final FaqRepository faqRepository;
    private final FaqCategoryRepository faqCategoryRepository;

    public FaqCommandService(FaqRepository faqRepository, FaqCategoryRepository faqCategoryRepository) {
        this.faqRepository = faqRepository;
        this.faqCategoryRepository = faqCategoryRepository;
    }

    @Override
    public Long createFaq(FaqCreateCommand command) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(command.faqCategoryId());
        validateCategoryExists(faqCategoryId);

        Faq faq = Faq.of(faqCategoryId, command.question(), command.answer(), command.sort(), command.visible());
        Faq saved = faqRepository.save(faq);
        return saved.getFaqId().value();
    }

    @Override
    public void updateFaq(FaqUpdateCommand command) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(command.faqCategoryId());
        validateCategoryExists(faqCategoryId);

        FaqId faqId = FaqId.of(command.faqId());
        Faq faq = findFaqOrThrow(faqId);

        faq.update(faqCategoryId, command.question(), command.answer(), command.sort(), command.visible());
        faqRepository.save(faq);
    }

    @Override
    public void deleteFaq(FaqDeleteCommand command) {
        FaqId faqId = FaqId.of(command.faqId());
        Faq faq = findFaqOrThrow(faqId);

        faq.delete();
        faqRepository.save(faq);
    }

    private Faq findFaqOrThrow(FaqId faqId) {
        return faqRepository.findById(faqId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FAQ_NOT_FOUND));
    }

    private void validateCategoryExists(FaqCategoryId faqCategoryId) {
        faqCategoryRepository.findById(faqCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FAQ_CATEGORY_NOT_FOUND));
    }
}
