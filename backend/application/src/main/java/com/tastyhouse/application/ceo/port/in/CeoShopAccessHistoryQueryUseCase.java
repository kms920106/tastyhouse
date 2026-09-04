package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDate;

import com.tastyhouse.application.shop.port.out.ShopCeoAssignmentHistoryResult;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 점주 가게 접속 이력 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code CeoShopAccessHistoryQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
@CeoApp
public interface CeoShopAccessHistoryQueryUseCase {

    PageResult<ShopCeoAssignmentHistoryResult> getShopAccessHistories(
        Long ceoId,
        String actionType,
        Long shopId,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    );
}
