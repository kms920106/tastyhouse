package com.tastyhouse.application.ceo.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseCommandUseCase;
import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseCreateCommand;
import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseDeleteCommand;
import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseUpdateCommand;
import com.tastyhouse.domain.ceo.service.CeoReplyPhraseService;

/**
 * 자주 쓰는 문구 명령 서비스(CQRS command 측).
 *
 * <p>5개 상한·소유권 검증·금칙어 검수는 모두 도메인 서비스({@link CeoReplyPhraseService})가 소유하고,
 * 이 서비스는 트랜잭션 경계만 책임진다.
 *
 * <p><b>{@code ShopOwnershipValidator}를 쓰지 않는다</b> — 문구는 가게가 아니라 점주 계정에 귀속되어
 * {@code shopId}가 경로에 없고, 인가는 문구의 {@code ceoId}와 토큰의 {@code ceoId} 일치로 도메인 서비스가
 * 수행한다.
 *
 * <p>{@code ..query..}를 주입하지 않는다(CQRS 교차 주입 금지). 그래서 등록은 식별자만 반환하고, 목록이
 * 필요하면 컨트롤러가 {@link CeoReplyPhraseQueryService}로 재조회한다.
 */
@Service
@CeoApp
@Transactional
public class CeoReplyPhraseCommandService implements CeoReplyPhraseCommandUseCase {

    private final CeoReplyPhraseService ceoReplyPhraseService;

    public CeoReplyPhraseCommandService(CeoReplyPhraseService ceoReplyPhraseService) {
        this.ceoReplyPhraseService = ceoReplyPhraseService;
    }

    /**
     * 자주 쓰는 문구를 등록한다.
     *
     * @return 생성된 문구 식별자
     */
    @Override
    public Long register(CeoReplyPhraseCreateCommand command) {
        return ceoReplyPhraseService.register(command.ceoId(), command.name(), command.content());
    }

    /**
     * 자주 쓰는 문구의 이름과 내용을 수정한다.
     */
    @Override
    public void modify(CeoReplyPhraseUpdateCommand command) {
        ceoReplyPhraseService.modify(
            command.ceoId(),
            command.replyPhraseId(),
            command.name(),
            command.content()
        );
    }

    /**
     * 자주 쓰는 문구를 삭제한다.
     */
    @Override
    public void remove(CeoReplyPhraseDeleteCommand command) {
        ceoReplyPhraseService.remove(command.ceoId(), command.replyPhraseId());
    }
}
