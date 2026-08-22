package com.tastyhouse.adminapi.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.service.ProductImageApprovalService;
import com.tastyhouse.domain.product.service.ProductRepresentativeApprovalService;
import com.tastyhouse.domain.product.service.ProductVegetarianApprovalService;
import com.tastyhouse.domain.product.vo.ProductImageChangeRequestId;
import com.tastyhouse.domain.product.vo.ProductRepresentativeRequestId;
import com.tastyhouse.domain.product.vo.ProductVegetarianRequestId;

/**
 * 메뉴 이미지·채식·사장님 추천 승인요청 검수 변경 서비스(CQRS command 측).
 *
 * <p>승인 시 요청 상태 전이와 메뉴 반영(이미지 목록 추가 / {@code vegetarianType} 적용 / {@code representative} 켜기)이 한 트랜잭션에서
 * 함께 일어나야 하는 원자 연산은 도메인 서비스가 담당한다(요청자 ceo·검수자 admin 공유 규칙). 이 서비스는
 * 트랜잭션 경계와 식별자 승격(Long → ID VO)만 책임진다.
 */
@Service
@Transactional
public class ProductApprovalCommandService {

    private final ProductImageApprovalService productImageApprovalService;
    private final ProductVegetarianApprovalService productVegetarianApprovalService;
    private final ProductRepresentativeApprovalService productRepresentativeApprovalService;

    public ProductApprovalCommandService(
        ProductImageApprovalService productImageApprovalService,
        ProductVegetarianApprovalService productVegetarianApprovalService,
        ProductRepresentativeApprovalService productRepresentativeApprovalService
    ) {
        this.productImageApprovalService = productImageApprovalService;
        this.productVegetarianApprovalService = productVegetarianApprovalService;
        this.productRepresentativeApprovalService = productRepresentativeApprovalService;
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

    /**
     * 사장님 추천 지정을 승인한다 — {@code Product.representative}가 켜진다.
     *
     * <p>도메인 서비스가 승인 시점에 개수 제한과 이미지 요건을 <b>다시</b> 검증한다. 신청 이후 대기가
     * 길어지는 사이 가게 상태가 달라질 수 있고, 컬럼을 켜는 지점이 여기뿐이므로 최종 방어선도 여기다.
     */
    public void approveRepresentative(Long id) {
        ProductRepresentativeRequestId requestId = ProductRepresentativeRequestId.of(id);
        productRepresentativeApprovalService.approve(requestId);
    }

    public void rejectRepresentative(Long id, String rejectReason) {
        ProductRepresentativeRequestId requestId = ProductRepresentativeRequestId.of(id);
        productRepresentativeApprovalService.reject(requestId, rejectReason);
    }
}
