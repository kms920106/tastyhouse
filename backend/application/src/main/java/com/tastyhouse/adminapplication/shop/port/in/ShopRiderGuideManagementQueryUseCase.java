package com.tastyhouse.adminapplication.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopRiderGuideHistoryResult;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideListItemResult;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideResult;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 가게 라이더 가이드 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopRiderGuideQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p><b>챕터 06</b> — 반환 타입은 Swagger를 아는 {@code *Response}가 아니라 프레임워크-프리
 * {@code *Result}다. 상세는 안내 본문과 변경 이력이 서로 다른 조회라 둘을 함께 담은
 * {@link ShopRiderGuideDetail}로 넘기고, 중첩 Response 조립은 컨트롤러가 담당한다.
 */
public interface ShopRiderGuideManagementQueryUseCase {

    PageResult<ShopRiderGuideListItemResult> getRiderGuides(
        String shopName,
        Boolean hasVisitGuide,
        int page,
        int size
    );

    ShopRiderGuideDetail getRiderGuide(Long shopId);

    /**
     * 라이더 안내 상세 조회 결과 — 안내 본문({@code guide})과 변경 이력({@code histories})의 묶음.
     *
     * <p>두 값은 읽기 포트의 서로 다른 메서드에서 오므로 단일 {@code *Result}가 존재하지 않는다.
     * 컨트롤러가 인자 두 개를 따로 받는 대신 이 묶음을 받아 중첩 Response를 조립한다.
     */
    record ShopRiderGuideDetail(
        ShopRiderGuideResult guide,
        List<ShopRiderGuideHistoryResult> histories
    ) {
    }
}
