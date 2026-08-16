package com.tastyhouse.domain.ceo.port;

/**
 * 자주 쓰는 문구 텍스트 검수 출력 포트.
 *
 * <p>실제 검수 규칙(금칙어 목록 대조)은 shop 컨텍스트의 {@code ProhibitedWordValidator}가 소유하고
 * 있으며, 이 포트의 어댑터가 그것을 그대로 호출한다 — 규칙을 복제하지 않는다. 액터·화면과 무관하게
 * 같은 금칙어 정책이 적용되어야 하므로 재사용이 맞다.
 *
 * <p><b>그럼에도 ceo 도메인이 {@code ProhibitedWordValidator}를 직접 부르지 않는 이유</b>는 컨텍스트
 * 경계다. 컨텍스트 간 참조는 ID VO·도메인 이벤트·출력 포트로만 허용되고 타 컨텍스트의
 * {@code service} 직접 import는 금지되어 있다({@code ContextBoundaryTest}). 기존에 같은 검증기를 직접
 * import하는 도메인 서비스들이 있으나 그것은 규칙 도입 이전 코드로 봉인된 것이라 선례로 삼지 않는다.
 *
 * <p>위반 시 {@code BusinessException(SHOP_TEXT_PROHIBITED_WORD)}(400)을 던진다 — 응답 계약은 가게소개·
 * 사장님 답변과 동일하다.
 */
public interface ReplyPhraseTextValidator {

    /**
     * 문구 내용에 등록할 수 없는 표현이 있는지 검수한다. 문제가 없으면 아무 일도 하지 않는다.
     */
    void validate(String text);
}
