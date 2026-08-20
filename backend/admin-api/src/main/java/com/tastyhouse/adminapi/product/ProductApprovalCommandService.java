package com.tastyhouse.adminapi.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.service.ProductImageApprovalService;
import com.tastyhouse.domain.product.service.ProductVegetarianApprovalService;
import com.tastyhouse.domain.product.vo.ProductImageChangeRequestId;
import com.tastyhouse.domain.product.vo.ProductVegetarianRequestId;

/**
 * 메뉴 이미지·채식 승인요청 검수 변경 서비스(CQRS command 측).
 *
 * <p>승인 시 요청 상태 전이와 메뉴 반영(이미지 목록 추가 / {@code vegetarianType} 적용)이 한 트랜잭션에서
 * 함께 일어나야 하는 원자 연산은 도메인 서비스가 담당한다(요청자 ceo·검수자 admin 공유 규칙). 이 서비스는
 * 트랜잭션 경계와 식별자 승격(Long → ID VO)만 책임진다.
 */
@Service
@Transactional
public class ProductApprovalCommandService {

    private final ProductImageApprovalService productImageApprovalService;
    private final ProductVegetarianApprovalService productVegetarianApprovalService;

    public ProductApprovalCommandService(
        ProductImageApprovalService productImageApprovalService,
        ProductVegetarianApprovalService productVegetarianApprovalService
    ) {
        this.productImageApprovalService = productImageApprovalService;
        this.productVegetarianApprovalService = productVegetarianApprovalService;
    }

    public void approveImageChange(Long id) {
        ProductImageChangeRequestId requestId = ProductImageChangeRequestId.of(id);
        productImageApprovalService.approve(requestId);
    }

    public void rejectImageChange(Long id, String rejectReason) {
        ProductImageChangeRequestId requestId = ProductImageChangeRequestId.of(id);
        productImageApprovalService.reject(requestId, rejectReason);
    }

    public void approveVegetarian(Long id) {
        ProductVegetarianRequestId requestId = ProductVegetarianRequestId.of(id);
        productVegetarianApprovalService.approve(requestId);
    }

    public void rejectVegetarian(Long id, String rejectReason) {
        ProductVegetarianRequestId requestId = ProductVegetarianRequestId.of(id);
        productVegetarianApprovalService.reject(requestId, rejectReason);
    }
}
