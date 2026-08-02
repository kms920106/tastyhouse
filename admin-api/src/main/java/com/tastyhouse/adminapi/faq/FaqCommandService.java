package com.tastyhouse.adminapi.faq;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.faq.domain.model.Faq;
import com.tastyhouse.domain.faq.domain.repository.FaqCategoryRepository;
import com.tastyhouse.domain.faq.domain.repository.FaqRepository;
import com.tastyhouse.domain.faq.domain.vo.FaqCategoryId;
import com.tastyhouse.domain.faq.domain.vo.FaqId;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * FAQ 항목 관리 command 서비스.
 *
 * <p>domain write 포트({@link FaqRepository}·{@link FaqCategoryRepository})만 주입해 생성·수정·삭제를
 * 수행한다. 조회는 {@link FaqQueryService}가 담당하며, 이 서비스는 infra query DAO를 주입하지 않는다.
 * {@link FaqCategoryRepository}는 소속 카테고리 존재 검증(단건 로드)에만 쓰이므로 write 포트 주입이
 * 맞다 — 두 애그리거트를 함께 save하지 않으므로 도메인 서비스로 하강시키지 않았다.
 *
 * <p>{@code Faq}는 순수 POJO라 더티 체킹이 없으므로 도메인 변경 후 명시적으로
 * {@code faqRepository.save(faq)}를 호출한다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class FaqCommandService {

    private final FaqRepository faqRepository;
    private final FaqCategoryRepository faqCategoryRepository;

    public Long createFaq(Long categoryId, String question, String answer, Integer sort, boolean visible) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(categoryId);
        validateCategoryExists(faqCategoryId);

        Faq faq = Faq.of(faqCategoryId, question, answer, sort, visible);
        Faq saved = faqRepository.save(faq);
        return saved.getFaqId().value();
    }

    public void updateFaq(Long id, Long categoryId, String question, String answer, Integer sort, boolean visible) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(categoryId);
        validateCategoryExists(faqCategoryId);

        FaqId faqId = FaqId.of(id);
        Faq faq = findFaqOrThrow(faqId);

        faq.update(faqCategoryId, question, answer, sort, visible);
        faqRepository.save(faq);
    }

    public void deleteFaq(Long id) {
        FaqId faqId = FaqId.of(id);
        Faq faq = findFaqOrThrow(faqId);

        faq.delete();
        faqRepository.save(faq);
    }

    private Faq findFaqOrThrow(FaqId faqId) {
        return faqRepository.findById(faqId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.FAQ_NOT_FOUND));
    }

    private void validateCategoryExists(FaqCategoryId faqCategoryId) {
        faqCategoryRepository.findById(faqCategoryId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.FAQ_CATEGORY_NOT_FOUND));
    }
}
