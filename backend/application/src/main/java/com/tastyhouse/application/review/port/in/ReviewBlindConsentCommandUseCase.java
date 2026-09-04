package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

/**
 * 게시중단 리뷰 삭제 동의·거부 쓰기 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ReviewBlindConsentCommandService})을 알지 않는다.
 */
@WebApp
public interface ReviewBlindConsentCommandUseCase {

    void consent(ReviewBlindConsentCommand command);

    void reject(ReviewBlindRejectCommand command);
}
