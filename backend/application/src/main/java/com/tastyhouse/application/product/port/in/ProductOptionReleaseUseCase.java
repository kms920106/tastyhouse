package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.product.port.out.ProductAvailabilityChangeView;

/**
 * 옵션 품절·숨김 해제 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현을 알지 않는다.
 */
@CeoApp
public interface ProductOptionReleaseUseCase {

    ProductAvailabilityChangeView releaseOptions(ProductOptionReleaseCommand command);
}
