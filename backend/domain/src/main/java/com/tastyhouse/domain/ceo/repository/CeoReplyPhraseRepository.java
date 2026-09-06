package com.tastyhouse.domain.ceo.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.ceo.model.CeoReplyPhrase;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.ceo.vo.CeoReplyPhraseId;

/**
 * 자주 쓰는 문구 write 포트.
 *
 * <p>{@code findAllByCeoId}·{@code countByCeoId}는 조회처럼 보이지만 표현 목적이 아니라 command 경로
 * 전용이다 — {@code countByCeoId}는 "점주당 5개 이하"라는 상한 검증과 신규 {@code sort} 산출에,
 * {@code findAllByCeoId}는 그 검증에 필요한 보유 목록 로드에 쓰인다(write 포트 잔류 판정 기준).
 * 화면 목록 조회는 {@code CeoReplyPhraseQueryDao}가 담당한다.
 */
public interface CeoReplyPhraseRepository {

    Optional<CeoReplyPhrase> findById(CeoReplyPhraseId ceoReplyPhraseId);

    List<CeoReplyPhrase> findAllByCeoId(CeoId ceoId);

    long countByCeoId(CeoId ceoId);

    CeoReplyPhrase save(CeoReplyPhrase ceoReplyPhrase);

    void delete(CeoReplyPhrase ceoReplyPhrase);
}
