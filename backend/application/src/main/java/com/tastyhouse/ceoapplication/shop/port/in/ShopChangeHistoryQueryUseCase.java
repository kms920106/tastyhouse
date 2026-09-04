package com.tastyhouse.ceoapplication.shop.port.in;

import java.time.LocalDate;
import java.util.List;

import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopChangeHistoryResult;
import com.tastyhouse.application.shop.port.out.ShopChangeCategoryResult;

/**
 * 가게 변경 이력 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopChangeHistoryQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ShopChangeHistoryQueryUseCase {

    PageResult<ShopChangeHistoryResult> getChangeHistories(
        Long ceoId,
        Long shopId,
        String category,
        String changeType,
        LocalDate changedDate,
        int page,
        int size
    );

    List<ShopChangeCategoryResult> getChangeHistoryTypes();
}
