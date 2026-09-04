package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.review.port.out.ReviewBlindNoticeResult;
import com.tastyhouse.application.shared.marker.WebApp;

/**
 * 리뷰 블라인드 동의 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ReviewBlindConsentQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p><b>챕터 10</b>에서 반환 타입이 Response에서 공용 읽기 계약으로 바뀌었다. 이 계약은 게시중단 사유를
 * 도메인 enum {@code ReviewBlindReason}으로 나르지만, 강등에 필요한 {@code name()}·{@code getDescription()}이
 * 모두 api 모듈의 허용 accessor라 컨트롤러가 그대로 조립할 수 있어 전용 View를 만들지 않았다.
 *
 * <p>인가 재검증(작성자 본인 여부)은 <b>서비스가 끝낸다</b> — 이 메서드가 값을 돌려주었다는 것 자체가
 * 그 판정을 통과했다는 뜻이다.
 */
@WebApp
public interface ReviewBlindConsentQueryUseCase {

    ReviewBlindNoticeResult getBlindNotice(Long reviewId, Long memberId);
}
