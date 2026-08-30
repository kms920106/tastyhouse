package com.tastyhouse.ceoapplication.product.port.in;

import com.tastyhouse.ceoapplication.product.response.ProductAvailabilityChangeResponse;

/**
 * 옵션 품절 기간 변경 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현을 알지 않는다.
 */
public interface ProductOptionSoldOutUntilChangeUseCase {

    ProductAvailabilityChangeResponse changeOptionsSoldOutUntil(ProductOptionSoldOutUntilChangeCommand command);
}
