package com.tastyhouse.ceoapi.review.application.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapi.review.application.port.in.ReviewOwnerReplyCommandUseCase;
import com.tastyhouse.ceoapi.review.application.port.in.ReviewOwnerReplyCreateCommand;
import com.tastyhouse.ceoapi.review.application.port.in.ReviewOwnerReplyDeleteCommand;
import com.tastyhouse.ceoapi.review.application.port.in.ReviewOwnerReplyUpdateCommand;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.domain.review.service.ReviewOwnerReplyService;

/**
 * 사장님 답변 명령 서비스(CQRS command 측).
 *
 * <p>리뷰-가게 일치 재검증·금칙어 검수·리뷰당 1건 제약은 모두 도메인 서비스
 * ({@link ReviewOwnerReplyService})가 소유하고, 이 서비스는 가게 소유권 검증과 트랜잭션 경계만 책임진다.
 *
 * <p>{@code ..query..}를 주입하지 않는다(CQRS 교차 주입 금지). 그래서 등록은 식별자만 반환하고, 상세가
 * 필요하면 컨트롤러가 {@link ShopReviewQueryService}로 재조회한다.
 */
@Service
@Transactional
public class ReviewOwnerReplyCommandService implements ReviewOwnerReplyCommandUseCase {

    private final ReviewOwnerReplyService reviewOwnerReplyService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ReviewOwnerReplyCommandService(
        ReviewOwnerReplyService reviewOwnerReplyService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.reviewOwnerReplyService = reviewOwnerReplyService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 사장님 답변을 등록한다.
     *
     * <p>기한(리뷰 작성일 + 30일) 판정 기준일인 {@code LocalDate.now()}는 <b>이 계층이 해석해</b> 도메인
     * 서비스에 넘긴다 — domain-module은 프레임워크-프리라 시계를 주입받을 수 없고, 도메인이 직접
     * {@code now()}를 부르면 단위 테스트에서 기한을 고정할 수 없기 때문이다.
     *
     * @return 생성된 답변 식별자
     */
    @Override
    public Long register(ReviewOwnerReplyCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return reviewOwnerReplyService.register(shopId, command.reviewId(), ceoId, command.content(), LocalDate.now());
    }

    /**
     * 사장님 답변 내용을 수정한다.
     */
    @Override
    public void modify(ReviewOwnerReplyUpdateCommand command) {
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(command.ceoId(), shopId);
        reviewOwnerReplyService.modify(shopId, command.reviewId(), command.content());
    }

    /**
     * 사장님 답변을 삭제한다.
     */
    @Override
    public void remove(ReviewOwnerReplyDeleteCommand command) {
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(command.ceoId(), shopId);
        reviewOwnerReplyService.remove(shopId, command.reviewId());
    }
}
