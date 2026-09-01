package com.tastyhouse.ceoapplication.product.port.in;

import com.tastyhouse.ceoapplication.product.port.out.ProductAvailabilityChangeView;

/**
 * 메뉴 품절 처리 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현을 알지 않는다.
 */
public interface ProductSoldOutUseCase {

    ProductAvailabilityChangeView markProductsSoldOut(ProductSoldOutCommand command);
}
