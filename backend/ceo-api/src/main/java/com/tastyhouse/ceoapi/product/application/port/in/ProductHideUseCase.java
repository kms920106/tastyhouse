package com.tastyhouse.ceoapi.product.application.port.in;

import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductAvailabilityChangeResponse;

/**
 * 메뉴 숨김 처리 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현을 알지 않는다.
 */
public interface ProductHideUseCase {

    ProductAvailabilityChangeResponse hideProducts(ProductHideCommand command);
}
