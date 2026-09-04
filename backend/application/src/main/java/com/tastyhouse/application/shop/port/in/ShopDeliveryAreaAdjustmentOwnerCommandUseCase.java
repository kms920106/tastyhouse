package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.web.multipart.MultipartFile;

/**
 * 점주 배달지역 조정 신청 쓰기 인바운드 포트.
 *
 * <p>동의서 파일은 Command 필드가 아니라 별도 파라미터로 받는다({@code MultipartFile}은 경계 타입이
 * 아니므로 Command에 담지 않는다).
 */
@CeoApp
public interface ShopDeliveryAreaAdjustmentOwnerCommandUseCase {

    Long requestAdjustment(ShopDeliveryAreaAdjustmentCreateCommand command, MultipartFile file);
}
