package com.tastyhouse.ceoapplication.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductAvailabilityGroupResult;
import com.tastyhouse.application.product.port.out.ProductOptionAvailabilityGroupResult;

/**
 * 메뉴 품절/숨김 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ProductAvailabilityQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ProductAvailabilityQueryUseCase {

    List<ProductAvailabilityGroupResult> getProductAvailability(
        Long ceoId,
        Long shopId,
        String keyword,
        Boolean soldOutOnly,
        Boolean hiddenOnly
    );

    List<ProductOptionAvailabilityGroupResult> getProductOptionAvailability(
        Long ceoId,
        Long shopId,
        String keyword,
        Boolean soldOutOnly,
        Boolean hiddenOnly
    );
}
