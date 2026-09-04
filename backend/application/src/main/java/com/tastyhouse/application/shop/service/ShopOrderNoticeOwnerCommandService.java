package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.service.ShopOrderNoticeService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeOwnerCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeUpsertCommand;

/**
 * 점주용 주문안내 변경 서비스(CQRS command 측).
 *
 * <p>얇다 — 소유권 검증만 하고 나머지는 도메인 서비스에 넘긴다. 등록/수정 분기와 본문 검증이
 * {@link ShopOrderNoticeService}에 있는 이유는, 관리자 게시중단 경로(admin-api)가 같은 애그리거트를
 * 다루므로 규칙이 한 곳에 있어야 두 모듈이 어긋나지 않기 때문이다.
 *
 * <p><b>승인 절차가 없어 저장이 곧 노출이다.</b> 메뉴모음컷·사장님 추천처럼 {@code PENDING} 행을
 * 만들지 않으므로, 이 메서드가 반환한 직후 손님 화면(C-3)에 문구가 나타난다. 규정 위반은 관리자가
 * 사후에 게시중단한다.
 */
@Service
@CeoApp
@Transactional
public class ShopOrderNoticeOwnerCommandService implements ShopOrderNoticeOwnerCommandUseCase {

    private final ShopOrderNoticeService shopOrderNoticeService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopOrderNoticeOwnerCommandService(
        ShopOrderNoticeService shopOrderNoticeService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopOrderNoticeService = shopOrderNoticeService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 주문안내를 등록하거나 수정한다(가게당 1건 전체교체).
     *
     * <p>게시중단 상태는 유지된다 — 점주가 문구를 고쳤다는 사실만으로 관리자 조치가 풀리면 한 글자
     * 수정으로 게시중단을 무력화할 수 있다.
     */
    @Override
    public void upsertOrderNotice(ShopOrderNoticeUpsertCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String content = command.content();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopOrderNoticeService.upsert(ShopId.of(shopId), content);
    }
}
