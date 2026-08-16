package com.tastyhouse.domain.ceo.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.ceo.model.CeoReplyPhrase;
import com.tastyhouse.domain.ceo.port.ReplyPhraseTextValidator;
import com.tastyhouse.domain.ceo.repository.CeoReplyPhraseRepository;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.ceo.vo.CeoReplyPhraseId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.model.ProhibitedWord;
import com.tastyhouse.domain.shop.repository.ProhibitedWordRepository;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 자주 쓰는 문구 불변식 단위 테스트. write 포트와 금칙어 포트를 fake로 대체해 Spring/DB 없이 판정
 * 로직만 검증한다.
 */
class CeoReplyPhraseServiceTest {

    private static final Long OWNER_CEO_ID = 1L;
    private static final Long OTHER_CEO_ID = 2L;

    private FakeCeoReplyPhraseRepository ceoReplyPhraseRepository;
    private CeoReplyPhraseService ceoReplyPhraseService;

    /**
     * 자주 쓰는 문구 write 포트의 인메모리 fake.
     *
     * <p>{@code save}가 신규 저장 시 <b>새 인스턴스를 반환</b>하는 것까지 실제 어댑터와 같게 재현한다 —
     * fake가 in-place로 id를 채우면 "반환값을 재할당하지 않아 식별자가 없는 채로 이어지는" 결함이 테스트에
     * 드러나지 않는다.
     */
    private static class FakeCeoReplyPhraseRepository implements CeoReplyPhraseRepository {

        private final Map<Long, CeoReplyPhrase> phrases = new HashMap<>();
        private long sequence = 0L;

        @Override
        public Optional<CeoReplyPhrase> findById(CeoReplyPhraseId ceoReplyPhraseId) {
            return Optional.ofNullable(phrases.get(ceoReplyPhraseId.value()));
        }

        @Override
        public List<CeoReplyPhrase> findAllByCeoId(CeoId ceoId) {
            return phrases.values().stream()
                .filter(phrase -> phrase.getCeoId().equals(ceoId))
                .sorted(Comparator.comparingInt(CeoReplyPhrase::getSort))
                .toList();
        }

        @Override
        public long countByCeoId(CeoId ceoId) {
            return phrases.values().stream()
                .filter(phrase -> phrase.getCeoId().equals(ceoId))
                .count();
        }

        @Override
        public CeoReplyPhrase save(CeoReplyPhrase ceoReplyPhrase) {
            if (ceoReplyPhrase.getId() != null) {
                phrases.put(ceoReplyPhrase.getId(), ceoReplyPhrase);
                return ceoReplyPhrase;
            }

            CeoReplyPhrase persisted = CeoReplyPhrase.reconstitute(
                ++sequence,
                ceoReplyPhrase.getCeoId(),
                ceoReplyPhrase.getName(),
                ceoReplyPhrase.getContent(),
                ceoReplyPhrase.getSort(),
                null,
                null
            );
            phrases.put(persisted.getId(), persisted);
            return persisted;
        }

        @Override
        public void delete(CeoReplyPhrase ceoReplyPhrase) {
            phrases.remove(ceoReplyPhrase.getId());
        }

        List<CeoReplyPhrase> all() {
            return new ArrayList<>(phrases.values());
        }
    }

    /**
     * 금칙어 테이블을 대신하는 fake. 시드와 동일하게 "전화주문" 하나만 담는다.
     */
    private static class FakeProhibitedWordRepository implements ProhibitedWordRepository {

        @Override
        public List<ProhibitedWord> findAll() {
            return List.of(ProhibitedWord.reconstitute(1L, "전화주문", "전화 주문 유도"));
        }
    }

    @BeforeEach
    void setUp() {
        ceoReplyPhraseRepository = new FakeCeoReplyPhraseRepository();
        // 검수 포트에 실제 ProhibitedWordValidator를 물려, 운영과 같은 판정 규칙을 태운다 — 포트를
        // 무조건 통과하는 스텁으로 바꾸면 "금칙어가 실제로 걸리는가"를 검증하지 못한다.
        ReplyPhraseTextValidator replyPhraseTextValidator =
            new ProhibitedWordValidator(new FakeProhibitedWordRepository())::validate;
        ceoReplyPhraseService = new CeoReplyPhraseService(
            ceoReplyPhraseRepository,
            replyPhraseTextValidator
        );
    }

    @Test
    @DisplayName("5개까지는 등록되고 6번째는 상한 초과로 거부된다")
    void register_throws_whenPhraseCountReachesLimit() {
        for (int i = 0; i < 5; i++) {
            ceoReplyPhraseService.register(OWNER_CEO_ID, "문구" + i, "감사합니다 " + i);
        }

        assertThat(ceoReplyPhraseRepository.countByCeoId(CeoId.of(OWNER_CEO_ID))).isEqualTo(5L);
        assertThatThrownBy(() -> ceoReplyPhraseService.register(OWNER_CEO_ID, "여섯번째", "감사합니다"))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CEO_REPLY_PHRASE_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("상한은 점주별로 센다 — 다른 점주가 5개를 채워도 내 등록은 막히지 않는다")
    void register_countsLimitPerCeo() {
        for (int i = 0; i < 5; i++) {
            ceoReplyPhraseService.register(OTHER_CEO_ID, "문구" + i, "감사합니다 " + i);
        }

        Long registeredId = ceoReplyPhraseService.register(OWNER_CEO_ID, "내 문구", "감사합니다");

        assertThat(registeredId).isNotNull();
        assertThat(ceoReplyPhraseRepository.countByCeoId(CeoId.of(OWNER_CEO_ID))).isEqualTo(1L);
    }

    @Test
    @DisplayName("등록 시 정렬 순서는 보유 건수로 서버가 채운다")
    void register_assignsSortFromCurrentCount() {
        ceoReplyPhraseService.register(OWNER_CEO_ID, "첫번째", "감사합니다");
        ceoReplyPhraseService.register(OWNER_CEO_ID, "두번째", "또 오세요");

        assertThat(ceoReplyPhraseRepository.findAllByCeoId(CeoId.of(OWNER_CEO_ID)))
            .extracting(CeoReplyPhrase::getSort)
            .containsExactly(0, 1);
    }

    @Test
    @DisplayName("다른 점주의 문구는 수정할 수 없다")
    void modify_throws_whenPhraseBelongsToOtherCeo() {
        Long phraseId = ceoReplyPhraseService.register(OWNER_CEO_ID, "내 문구", "감사합니다");

        assertThatThrownBy(() -> ceoReplyPhraseService.modify(OTHER_CEO_ID, phraseId, "가로채기", "고칩니다"))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CEO_REPLY_PHRASE_ACCESS_DENIED);
    }

    @Test
    @DisplayName("다른 점주의 문구는 삭제할 수 없다")
    void remove_throws_whenPhraseBelongsToOtherCeo() {
        Long phraseId = ceoReplyPhraseService.register(OWNER_CEO_ID, "내 문구", "감사합니다");

        assertThatThrownBy(() -> ceoReplyPhraseService.remove(OTHER_CEO_ID, phraseId))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CEO_REPLY_PHRASE_ACCESS_DENIED);
        assertThat(ceoReplyPhraseRepository.all()).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 문구를 수정하면 404로 거부된다")
    void modify_throws_whenPhraseNotFound() {
        assertThatThrownBy(() -> ceoReplyPhraseService.modify(OWNER_CEO_ID, 999L, "이름", "내용"))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CEO_REPLY_PHRASE_NOT_FOUND);
    }

    @Test
    @DisplayName("금칙어가 포함된 내용은 등록되지 않는다")
    void register_throws_whenContentContainsProhibitedWord() {
        assertThatThrownBy(() ->
            ceoReplyPhraseService.register(OWNER_CEO_ID, "안내", "전화주문 주시면 할인해드립니다"))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_TEXT_PROHIBITED_WORD);
        assertThat(ceoReplyPhraseRepository.all()).isEmpty();
    }

    @Test
    @DisplayName("금칙어가 포함된 내용으로는 수정되지 않는다 — 기존 내용이 유지된다")
    void modify_throws_whenContentContainsProhibitedWord() {
        Long phraseId = ceoReplyPhraseService.register(OWNER_CEO_ID, "안내", "감사합니다");

        assertThatThrownBy(() ->
            ceoReplyPhraseService.modify(OWNER_CEO_ID, phraseId, "안내", "전화주문 주세요"))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_TEXT_PROHIBITED_WORD);
        assertThat(ceoReplyPhraseRepository.findById(CeoReplyPhraseId.of(phraseId)))
            .get()
            .extracting(CeoReplyPhrase::getContent)
            .isEqualTo("감사합니다");
    }

    @Test
    @DisplayName("문구 이름을 입력하지 않아도 등록된다 — 이름은 선택 항목이다")
    void register_allows_whenNameIsNull() {
        Long phraseId = ceoReplyPhraseService.register(OWNER_CEO_ID, null, "소중한 리뷰 감사합니다");

        assertThat(ceoReplyPhraseRepository.findById(CeoReplyPhraseId.of(phraseId)))
            .get()
            .extracting(CeoReplyPhrase::getName)
            .isNull();
    }

    @Test
    @DisplayName("수정으로 문구 이름을 비울 수 있다 — 비우면 화면이 내용 앞부분을 대신 표시한다")
    void modify_allows_clearingName() {
        Long phraseId = ceoReplyPhraseService.register(OWNER_CEO_ID, "감사 인사", "감사합니다");

        ceoReplyPhraseService.modify(OWNER_CEO_ID, phraseId, null, "다시 찾아주세요");

        assertThat(ceoReplyPhraseRepository.findById(CeoReplyPhraseId.of(phraseId)))
            .get()
            .satisfies(phrase -> {
                assertThat(phrase.getName()).isNull();
                assertThat(phrase.getContent()).isEqualTo("다시 찾아주세요");
            });
    }

    @Test
    @DisplayName("삭제 후 남은 문구의 정렬 순서는 다시 매기지 않는다 — 빈 번호가 생겨도 순서는 유지된다")
    void remove_doesNotReorderRemainingPhrases() {
        Long first = ceoReplyPhraseService.register(OWNER_CEO_ID, "첫번째", "감사합니다");
        ceoReplyPhraseService.register(OWNER_CEO_ID, "두번째", "또 오세요");

        ceoReplyPhraseService.remove(OWNER_CEO_ID, first);

        assertThat(ceoReplyPhraseRepository.findAllByCeoId(CeoId.of(OWNER_CEO_ID)))
            .extracting(CeoReplyPhrase::getSort)
            .containsExactly(1);
    }
}
