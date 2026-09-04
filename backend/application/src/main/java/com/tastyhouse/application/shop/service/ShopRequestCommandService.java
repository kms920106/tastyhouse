package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.service.ShopRequestCancelService;
import com.tastyhouse.domain.shop.service.ShopRequestCommentService;
import com.tastyhouse.application.shop.port.in.ShopRequestCancelCommand;
import com.tastyhouse.application.shop.port.in.ShopRequestCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopRequestCommentOwnerCreateCommand;

/**
 * 점주용 요청처리 현황 명령 서비스(CQRS command 측).
 *
 * <p>취소 가능 조건(PENDING만)과 스레드 소유 가게 재검증은 도메인 서비스가 담당하고, 이 서비스는 소유권
 * 검증·트랜잭션 경계만 책임진다. 취소는 원본 애그리거트 전이와 인덱스 동기화가 <b>한 트랜잭션</b>에서
 * 일어나야 하므로 그 경계를 여기서 선언한다.
 *
 * <p>{@code ..query..}를 주입하지 않는다(CQRS 교차 주입 금지). 그래서 취소는 응답 본문이 없고 댓글 작성은
 * 식별자만 반환하며, 상세가 필요하면 컨트롤러가 {@link ShopRequestQueryService}로 재조회한다.
 */
@Service
@CeoApp
@Transactional
public class ShopRequestCommandService implements ShopRequestCommandUseCase {

    private final ShopRequestCancelService shopRequestCancelService;
    private final ShopRequestCommentService shopRequestCommentService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopRequestCommandService(
        ShopRequestCancelService shopRequestCancelService,
        ShopRequestCommentService shopRequestCommentService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopRequestCancelService = shopRequestCancelService;
        this.shopRequestCommentService = shopRequestCommentService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 점주가 대기중인 요청을 취소한다.
     */
    @Override
    public void cancelRequest(ShopRequestCancelCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long requestId = command.requestId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopRequestCancelService.cancel(requestId, shopId);
    }

    /**
     * 점주가 요청건에 문의를 남긴다.
     *
     * @return 생성된 댓글 식별자
     */
    @Override
    public Long addComment(ShopRequestCommentOwnerCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long requestId = command.requestId();
        String content = command.content();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopRequestCommentService.addCommentByCeo(requestId, shopId, ceoId, content);
    }
}
