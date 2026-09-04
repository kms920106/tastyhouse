package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.web.multipart.MultipartFile;

/**
 * 메뉴 이미지 쓰기 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현을 알지 않는다.
 *
 * <p>{@code MultipartFile}은 command 필드가 아니라 별도 파라미터로 받는다 — 경계 타입이 아니기 때문이다.
 */
@CeoApp
public interface ProductImageCommandUseCase {

    Long requestImageChange(ProductImageChangeRequestCommand command, MultipartFile file);

    void reorderImages(ProductImageReorderCommand command);

    void deleteImage(ProductImageDeleteCommand command);
}
