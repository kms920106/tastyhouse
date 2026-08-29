package com.tastyhouse.adminapi.product.application.port.in;

/**
 * 상품 옵션그룹 등록 인바운드 포트.
 *
 * <p>{@code ProductCommandService}는 public 메서드가 8개라 연산 단위 인터페이스로 분해했다
 * (챕터 02 §4 per-operation 분해 기준: 7개 초과).
 */
public interface ProductOptionGroupCreateUseCase {

    Long createProductOptionGroup(ProductOptionGroupCreateCommand command);
}
