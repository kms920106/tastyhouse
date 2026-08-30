package com.tastyhouse.infrastructure.ceo.persistence;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.ceo.port.ReplyPhraseTextValidator;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;

/**
 * 자주 쓰는 문구 텍스트 검수 포트({@link ReplyPhraseTextValidator}) 어댑터.
 *
 * <p>검수 규칙 자체는 shop 컨텍스트가 소유한 {@link ProhibitedWordValidator}에 그대로 위임한다 —
 * 이 어댑터는 <b>규칙을 복제하지 않고 컨텍스트 경계만 건너게 해 주는 얇은 배선</b>이다. 덕분에 ceo
 * 도메인 서비스는 shop 컨텍스트의 {@code service} 패키지를 알지 않으면서도 같은 금칙어 정책을 쓴다
 * (경계 규칙은 {@code ContextBoundaryTest}, 포트+어댑터 선례는 {@code MemberReviewCountAdapter}).
 *
 * <p>주입받는 {@link ProhibitedWordValidator} 빈은 {@code ShopDomainConfig}가 캐싱 데코레이터로 감싼
 * 포트를 물려 등록한 것이므로, 검증마다 금칙어 전량을 DB에서 다시 읽지 않는다.
 */
@Component
public class ReplyPhraseProhibitedWordValidatorAdapter implements ReplyPhraseTextValidator {

    private final ProhibitedWordValidator prohibitedWordValidator;

    public ReplyPhraseProhibitedWordValidatorAdapter(ProhibitedWordValidator prohibitedWordValidator) {
        this.prohibitedWordValidator = prohibitedWordValidator;
    }

    @Override
    public void validate(String text) {
        prohibitedWordValidator.validate(text);
    }
}
