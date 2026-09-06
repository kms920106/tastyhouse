package com.tastyhouse.domain.ceo.service;

import com.tastyhouse.domain.ceo.model.CeoReplyPhrase;
import com.tastyhouse.domain.ceo.port.ReplyPhraseTextValidator;
import com.tastyhouse.domain.ceo.repository.CeoReplyPhraseRepository;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.ceo.vo.CeoReplyPhraseId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 자주 쓰는 문구 등록·수정·삭제 불변식(도메인 서비스).
 *
 * <p><b>5개 상한은 DB가 아니라 이 애플리케이션 코드가 강제한다 — 그래서 완전하지 않다.</b> MySQL에는
 * "한 점주당 행 5개 이하" 같은 행 수 제약을 걸 수단이 없으므로, 건수 조회
 * ({@link CeoReplyPhraseRepository#countByCeoId})와 삽입 사이의 경합을 막을 최종 방어선이 존재하지 않는다. 같은 점주가 동시에 등록 요청을 보내면 6개가
 * 될 수 있다. 이를 감수하는 이유는 (1) 이 목록이 답변 작성 시 골라 쓰는 <b>표시용</b>이라 6개가 되어도
 * 데이터 정합성이나 금전에 피해가 없고, (2) 이를 막으려면 점주 행에 비관적 잠금을 걸어야 하는데 그
 * 비용이 피해에 비해 과하기 때문이다. 한 개 초과한 상태가 발견되면 점주가 하나 지우면 그만이다.
 *
 * <p>소유권은 {@code shopId}가 아니라 <b>문구의 {@code ceoId}와 요청 점주의 일치</b>로 검증한다 —
 * 문구는 가게가 아니라 점주 계정에 귀속되므로 {@code ShopOwnershipValidator}가 개입할 자리가 없다.
 * 불일치는 404가 아니라 {@code CEO_REPLY_PHRASE_ACCESS_DENIED}(403)로 응답한다.
 *
 * <p>금칙어 검수는 기존 {@code ProhibitedWordValidator}(shop 컨텍스트)를 재사용한다(가게소개·사장님
 * 답변 선례) — 점주가 입력하고 결국 고객에게 노출되는 텍스트라는 성격이 같다. 다만 컨텍스트 경계상 타
 * 컨텍스트의 {@code service}를 직접 import할 수 없으므로({@code ContextBoundaryTest}), 출력 포트
 * {@link ReplyPhraseTextValidator}를 경유해 그 검증기에 위임한다. <b>등록·수정 시점에 검수</b>하므로 이
 * 문구를 실제로 답변에 넣을 때 {@code ReviewOwnerReplyService}가 한 번 더 검수하는 것과 중복되지만 그것이
 * 의도다 — 문구 등록 후 금칙어 목록이 늘어났을 수 있다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code CeoDomainConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는 ceo-api의
 * {@code CeoReplyPhraseCommandService}가 선언한다.
 */
public class CeoReplyPhraseService {

    /** 점주 1인당 등록 가능한 문구 개수 상한(앱 강제 — 위 Javadoc의 한계 참고). */
    private static final int MAX_PHRASE_COUNT = 5;

    private final CeoReplyPhraseRepository ceoReplyPhraseRepository;
    private final ReplyPhraseTextValidator replyPhraseTextValidator;

    public CeoReplyPhraseService(
        CeoReplyPhraseRepository ceoReplyPhraseRepository,
        ReplyPhraseTextValidator replyPhraseTextValidator
    ) {
        this.ceoReplyPhraseRepository = ceoReplyPhraseRepository;
        this.replyPhraseTextValidator = replyPhraseTextValidator;
    }

    /**
     * 자주 쓰는 문구를 등록한다.
     *
     * <p>{@code sort}는 클라이언트가 보내지 않고 <b>현재 보유 건수</b>를 그대로 순번으로 쓴다. 삭제 후
     * 재정렬을 하지 않으므로 번호에 빈 자리가 생길 수 있으나, 정렬은 순번의 대소로만 결정되므로 표시
     * 순서에는 영향이 없다.
     *
     * @param name 문구 이름. 미입력({@code null})이면 화면이 내용 앞부분을 대신 표시한다
     * @return 생성된 문구 식별자
     * @throws BusinessException 이미 {@value #MAX_PHRASE_COUNT}개면 {@code CEO_REPLY_PHRASE_LIMIT_EXCEEDED},
     *     금칙어가 포함되면 {@code SHOP_TEXT_PROHIBITED_WORD}
     */
    public Long register(Long ceoId, String name, String content) {
        CeoId ownerId = CeoId.of(ceoId);
        replyPhraseTextValidator.validate(content);

        long count = ceoReplyPhraseRepository.countByCeoId(ownerId);
        if (count >= MAX_PHRASE_COUNT) {
            throw new BusinessException(ErrorCode.CEO_REPLY_PHRASE_LIMIT_EXCEEDED);
        }

        CeoReplyPhrase saved = ceoReplyPhraseRepository.save(
            CeoReplyPhrase.of(ownerId, name, content, (int) count)
        );
        return saved.getId();
    }

    /**
     * 자주 쓰는 문구의 이름과 내용을 수정한다.
     *
     * @throws ResourceNotFoundException 문구가 없으면 {@code CEO_REPLY_PHRASE_NOT_FOUND}
     * @throws BusinessException 다른 점주의 문구면 {@code CEO_REPLY_PHRASE_ACCESS_DENIED},
     *     금칙어가 포함되면 {@code SHOP_TEXT_PROHIBITED_WORD}
     */
    public void modify(Long ceoId, Long phraseId, String name, String content) {
        replyPhraseTextValidator.validate(content);

        CeoReplyPhrase phrase = loadOwnPhrase(ceoId, phraseId);
        phrase.updateContent(name, content);
        ceoReplyPhraseRepository.save(phrase);
    }

    /**
     * 자주 쓰는 문구를 삭제한다(하드 삭제).
     *
     * <p>삭제 후 남은 문구의 {@code sort}를 재정렬하지 않는다 — 빈 번호가 생겨도 대소 관계가 유지되어
     * 표시 순서가 바뀌지 않으므로, 다른 행을 건드릴 이유가 없다.
     *
     * @throws ResourceNotFoundException 문구가 없으면 {@code CEO_REPLY_PHRASE_NOT_FOUND}
     * @throws BusinessException 다른 점주의 문구면 {@code CEO_REPLY_PHRASE_ACCESS_DENIED}
     */
    public void remove(Long ceoId, Long phraseId) {
        CeoReplyPhrase phrase = loadOwnPhrase(ceoId, phraseId);
        ceoReplyPhraseRepository.delete(phrase);
    }

    /**
     * 문구를 로드하고 그것이 요청 점주의 것임을 재검증한다.
     */
    private CeoReplyPhrase loadOwnPhrase(Long ceoId, Long phraseId) {
        CeoReplyPhrase phrase = ceoReplyPhraseRepository.findById(CeoReplyPhraseId.of(phraseId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CEO_REPLY_PHRASE_NOT_FOUND));
        if (!phrase.getCeoId().equals(CeoId.of(ceoId))) {
            throw new BusinessException(ErrorCode.CEO_REPLY_PHRASE_ACCESS_DENIED);
        }
        return phrase;
    }
}
