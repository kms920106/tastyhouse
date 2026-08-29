package com.tastyhouse.ceoapi.ceo.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapi.ceo.adapter.in.web.response.CeoReplyPhraseResponse;
import com.tastyhouse.ceoapi.ceo.application.port.in.CeoReplyPhraseQueryUseCase;
import com.tastyhouse.application.ceo.port.out.CeoReplyPhraseQueryPort;
import com.tastyhouse.application.ceo.port.out.CeoReplyPhraseResult;

/**
 * 자주 쓰는 문구 조회 서비스(CQRS query 측).
 *
 * <p><b>표시명({@code displayName}) 파생을 여기서 계산한다.</b> "이름이 비면 내용 앞부분을 보여준다"는
 * 화면 규칙이지 도메인 불변식이 아니고, 파생값을 DB에 저장하면 내용을 수정할 때 어긋난다. 그래서 도메인
 * 모델·엔티티·Result 어디에도 두지 않고 조회 시점에 매퍼가 만든다.
 *
 * <p>인가는 "토큰의 {@code ceoId}로만 필터한다"는 것 자체다 — 문구는 계정 단위라 가게에 종속되지 않으므로
 * {@code shopId}·소유권 검증이 없다.
 */
@Service
@Transactional(readOnly = true)
public class CeoReplyPhraseQueryService implements CeoReplyPhraseQueryUseCase {

    /** 이름 미입력 시 표시명으로 쓸 내용 앞부분의 길이(자). */
    private static final int DISPLAY_NAME_LENGTH = 20;

    /** 내용이 잘렸음을 나타내는 말줄임표. */
    private static final String ELLIPSIS = "…";

    private final CeoReplyPhraseQueryPort ceoReplyPhraseQueryPort;

    public CeoReplyPhraseQueryService(CeoReplyPhraseQueryPort ceoReplyPhraseQueryPort) {
        this.ceoReplyPhraseQueryPort = ceoReplyPhraseQueryPort;
    }

    /**
     * 내 자주 쓰는 문구 목록을 정렬 순서대로 조회한다. 5건 상한이라 페이징하지 않는다.
     */
    @Override
    public List<CeoReplyPhraseResponse> getReplyPhrases(Long ceoId) {
        return ceoReplyPhraseQueryPort.findReplyPhrases(ceoId).stream()
            .map(this::toReplyPhraseResponse)
            .toList();
    }

    private CeoReplyPhraseResponse toReplyPhraseResponse(CeoReplyPhraseResult result) {
        return CeoReplyPhraseResponse.from(
            result.id(),
            result.name(),
            resolveDisplayName(result.name(), result.content()),
            result.content(),
            result.sort(),
            result.createdAt()
        );
    }

    /**
     * 표시명을 정한다. 이름이 있으면 그대로 쓰고, 없으면 내용 앞 {@value #DISPLAY_NAME_LENGTH}자에
     * 말줄임표를 붙인다.
     *
     * <p>내용이 {@value #DISPLAY_NAME_LENGTH}자 이하면 잘린 것이 없으므로 말줄임표를 붙이지 않는다 —
     * 붙이면 뒤에 더 있다는 잘못된 인상을 준다.
     */
    private String resolveDisplayName(String name, String content) {
        if (name != null && !name.isBlank()) {
            return name;
        }
        if (content.length() <= DISPLAY_NAME_LENGTH) {
            return content;
        }
        return content.substring(0, DISPLAY_NAME_LENGTH) + ELLIPSIS;
    }
}
