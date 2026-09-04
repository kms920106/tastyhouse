package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

/**
 * 메뉴 이미지·채식·사장님 추천 승인요청 검수 쓰기 인바운드 포트.
 *
 * <p>승인요청 3종이 컨트롤러 하나를 공유하므로 인터페이스도 하나로 둔다(메서드 6개 — 분해 기준 미만).
 */
@AdminApp
public interface ProductApprovalCommandUseCase {

    void approveImageChange(ProductImageChangeApproveCommand command);

    void rejectImageChange(ProductImageChangeRejectCommand command);

    void approveVegetarian(ProductVegetarianApproveCommand command);

    void rejectVegetarian(ProductVegetarianRejectCommand command);

    void approveRepresentative(ProductRepresentativeApproveCommand command);

    void rejectRepresentative(ProductRepresentativeRejectCommand command);
}
