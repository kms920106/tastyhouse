package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.web.multipart.MultipartFile;

/**
 * 점주 매장 가격 인증 쓰기 인바운드 포트.
 *
 * <p>업로드 파일은 command가 아니라 별도 파라미터로 받는다(챕터 02 §6). 이 포트가
 * {@code MultipartFile}을 시그니처에 갖는 것은 그 예외 규정에 따른 것으로,
 * Command <b>필드</b>로 담는 것과는 구분된다.
 */
@CeoApp
public interface ShopStorePriceVerificationCommandUseCase {

    Long requestVerification(ShopStorePriceVerificationRequestCommand command, MultipartFile file);
}
