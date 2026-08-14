package com.tastyhouse.ceoapi.review;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.service.ReviewOwnerReplyService;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;

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
public class ReviewOwnerReplyCommandService {

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
     * @return 생성된 답변 식별자
     */
    public Long register(Long ceoId, Long shopId, Long reviewId, String content) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return reviewOwnerReplyService.register(shopId, reviewId, ceoId, content);
    }

    /**
     * 사장님 답변 내용을 수정한다.
     */
    public void modify(Long ceoId, Long shopId, Long reviewId, String content) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        reviewOwnerReplyService.modify(shopId, reviewId, content);
    }

    /**
     * 사장님 답변을 삭제한다.
     */
    public void remove(Long ceoId, Long shopId, Long reviewId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        reviewOwnerReplyService.remove(shopId, reviewId);
    }
}
