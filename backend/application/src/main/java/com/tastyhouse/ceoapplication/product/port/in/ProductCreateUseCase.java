package com.tastyhouse.ceoapplication.product.port.in;

/**
 * 메뉴 등록 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현을 알지 않는다.
 */
public interface ProductCreateUseCase {

    Long createProduct(ProductCreateCommand command);
}
