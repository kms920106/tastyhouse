package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

/**
 * 메뉴 정보 고객 의견 쓰기 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ProductFeedbackCommandService})을 알지 않는다.
 */
@WebApp
public interface ProductFeedbackCommandUseCase {

    Long submitFeedback(ProductFeedbackCreateCommand command);
}
