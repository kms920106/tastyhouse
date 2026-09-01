package com.tastyhouse.ceoapplication.ceo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.ceo.port.in.CeoReplyPhraseQueryUseCase;
import com.tastyhouse.application.ceo.port.out.CeoReplyPhraseQueryPort;
import com.tastyhouse.application.ceo.port.out.CeoReplyPhraseResult;

/**
 * 자주 쓰는 문구 조회 서비스(CQRS query 측).
 *
 * <p><b>표시명({@code displayName}) 파생은 이 서비스가 하지 않는다</b>(챕터 09에서 이동). "이름이 비면
 * 내용 앞부분을 보여준다"는 화면 규칙이지 도메인 불변식이 아니고, 파생값을 DB에 저장하면 내용을 수정할
 * 때 어긋난다 — 그래서 도메인 모델·엔티티·Result 어디에도 두지 않고, 표현 계약인
 * {@code CeoReplyPhraseResponse#from}이 조회 응답을 만들 때 파생시킨다.
 *
 * <p>인가는 "토큰의 {@code ceoId}로만 필터한다"는 것 자체다 — 문구는 계정 단위라 가게에 종속되지 않으므로
 * {@code shopId}·소유권 검증이 없다.
 */
@Service
@Transactional(readOnly = true)
public class CeoReplyPhraseQueryService implements CeoReplyPhraseQueryUseCase {

    private final CeoReplyPhraseQueryPort ceoReplyPhraseQueryPort;

    public CeoReplyPhraseQueryService(CeoReplyPhraseQueryPort ceoReplyPhraseQueryPort) {
        this.ceoReplyPhraseQueryPort = ceoReplyPhraseQueryPort;
    }

    /**
     * 내 자주 쓰는 문구 목록을 정렬 순서대로 조회한다. 5건 상한이라 페이징하지 않는다.
     */
    @Override
    public List<CeoReplyPhraseResult> getReplyPhrases(Long ceoId) {
        return ceoReplyPhraseQueryPort.findReplyPhrases(ceoId);
    }
}
