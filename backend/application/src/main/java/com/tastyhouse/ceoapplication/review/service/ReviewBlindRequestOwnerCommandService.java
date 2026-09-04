package com.tastyhouse.ceoapplication.review.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.review.port.in.ReviewBlindRequestCancelCommand;
import com.tastyhouse.ceoapplication.review.port.in.ReviewBlindRequestOwnerCommandUseCase;
import com.tastyhouse.ceoapplication.review.port.in.ReviewBlindRequestCreateCommand;
import com.tastyhouse.ceoapplication.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.review.model.ReviewBlindReason;
import com.tastyhouse.domain.review.service.ReviewBlindRequestService;

/**
 * 점주 리뷰 게시중단 요청 명령 서비스(CQRS command 측).
 *
 * <p>PENDING 중복 차단·{@code ETC} 상세사유 필수·요청처리 현황 인덱스 동기화는 모두 도메인 서비스
 * ({@link ReviewBlindRequestService})가 소유한다. 요청 접수와 인덱스 기록이 <b>한 트랜잭션</b>에서
 * 일어나야 하므로 그 경계를 이 서비스가 선언한다 — 기록이 유실되면 그 요청이 통합 요청처리 목록에서
 * 아예 보이지 않는다.
 */
@Service
@Transactional
public class ReviewBlindRequestOwnerCommandService implements ReviewBlindRequestOwnerCommandUseCase {

    private final ReviewBlindRequestService reviewBlindRequestService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ReviewBlindRequestOwnerCommandService(
        ReviewBlindRequestService reviewBlindRequestService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.reviewBlindRequestService = reviewBlindRequestService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 리뷰 게시중단을 요청한다.
     *
     * @return 생성된 요청 식별자
     */
    @Override
    public Long request(ReviewBlindRequestCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long reviewId = command.reviewId();
        String detailReason = command.detailReason();
        List<Long> attachmentFileIds = command.attachmentFileIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        ReviewBlindReason blindReason = ReviewBlindReason.from(command.reason());
        return reviewBlindRequestService.request(shopId, reviewId, ceoId, blindReason, detailReason, attachmentFileIds);
    }

    /**
     * 대기중인 게시중단 요청을 취소한다. 취소 후에는 같은 리뷰에 재요청할 수 있다.
     */
    @Override
    public void cancel(ReviewBlindRequestCancelCommand command) {
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(command.ceoId(), shopId);
        reviewBlindRequestService.cancel(command.blindRequestId(), shopId);
    }
}
