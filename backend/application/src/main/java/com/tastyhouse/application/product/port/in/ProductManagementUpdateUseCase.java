package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

/**
 * 관리자 상품 수정 인바운드 포트.
 *
 * <p>{@code ProductCommandService}는 public 메서드가 8개라 연산 단위 인터페이스로 분해했다
 * (챕터 02 §4 per-operation 분해 기준: 7개 초과).
 */
@AdminApp
public interface ProductManagementUpdateUseCase {

    void updateProduct(ProductManagementUpdateCommand command);
}
