package com.tastyhouse.domain.review.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.event.ReviewOwnerReplyCreatedEvent;
import com.tastyhouse.domain.review.model.ReviewOwnerReply;
import com.tastyhouse.domain.review.repository.ReviewOwnerReplyRepository;
import com.tastyhouse.domain.review.repository.ReviewRepository;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.review.vo.ReviewOwnerReplyId;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 사장님 답변 등록·수정·삭제 불변식(도메인 서비스).
 *
 * <p><b>대상 리뷰가 그 가게의 것인지 매번 역조회로 재검증한다</b>({@link #loadReviewOfShop}). 경로의
 * {@code shopId}로 소유권을 통과했더라도 {@code reviewId}는 별개의 하위 리소스이므로, 그것만 남의 것으로
 * 바꿔 보내면 다른 가게 리뷰에 답변을 달 수 있는 IDOR이 된다. 검증을 api 모듈에 두지 않고 여기 두는 이유는
 * 같은 규칙이 등록·수정·삭제 세 경로에 모두 필요해서다.
 *
 * <p>불일치를 404가 아니라 {@code SHOP_ACCESS_DENIED}(403)로 응답한다 — 리뷰는 web에 공개된 리소스라
 * 존재 자체가 비밀이 아니므로, 요청처리 현황({@code SHOP_REQUEST_NOT_FOUND} 404로 존재를 숨기는 쪽)과
 * 판단이 갈린다.
 *
 * <p>금칙어 검수는 기존 {@link ProhibitedWordValidator}를 재사용한다(가게소개·찾아오는길 선례) — 점주가
 * 입력하는 공개 텍스트라는 성격이 같고, 액터와 무관하게 같은 규칙이 적용되어야 하는 무상태 정책이다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code ReviewDomainConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는 api 모듈 CommandService가
 * 선언한다.
 */
public class ReviewOwnerReplyService {

    private final ReviewOwnerReplyRepository reviewOwnerReplyRepository;
    private final ReviewRepository reviewRepository;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final DomainEventPublisher domainEventPublisher;

    public ReviewOwnerReplyService(
        ReviewOwnerReplyRepository reviewOwnerReplyRepository,
        ReviewRepository reviewRepository,
        ProhibitedWordValidator prohibitedWordValidator,
        DomainEventPublisher domainEventPublisher
    ) {
        this.reviewOwnerReplyRepository = reviewOwnerReplyRepository;
        this.reviewRepository = reviewRepository;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * 사장님 답변을 등록한다.
     *
     * <p>{@code existsByReviewId} 검사로 409를 돌려주지만, 동시 요청의 최종 방어선은
     * {@code UNIQUE(review_id)}다 — 검사와 삽입 사이의 경합은 애플리케이션 코드로 막을 수 없다.
     *
     * <p><b>리뷰 작성일 + {@link ReviewOwnerReply#REPLY_PERIOD_DAYS}일이 지나면 등록할 수 없다</b>
     * ({@code REVIEW_OWNER_REPLY_PERIOD_EXPIRED}, 400). {@code modify}·{@code remove}에는 이 제한이
     * 없다 — {@link #validateWithinReplyPeriod} 참고.
     *
     * <p>성공 시 {@link ReviewOwnerReplyCreatedEvent}를 발행해 인앱 알림 적재를 트리거한다. 알림 적재는
     * AFTER_COMMIT 리스너가 맡으므로 그쪽이 실패해도 답변 등록 자체는 유효하다.
     *
     * @param today 기한 판정 기준일. 도메인이 {@code LocalDate.now()}를 직접 호출하지 않도록 호출부가
     *              해석해 넘긴다
     * @return 생성된 답변 식별자
     */
    public Long register(Long shopId, Long reviewId, Long ceoId, String content, LocalDate today) {
        ReviewId targetReviewId = ReviewId.of(reviewId);
        Review review = loadReviewOfShop(targetReviewId, shopId);
        validateWithinReplyPeriod(review, today);
        prohibitedWordValidator.validate(content);

        if (reviewOwnerReplyRepository.existsByReviewId(targetReviewId)) {
            throw new BusinessException(ErrorCode.REVIEW_OWNER_REPLY_ALREADY_EXISTS);
        }

        ReviewOwnerReply saved = reviewOwnerReplyRepository.save(
            ReviewOwnerReply.of(targetReviewId, ShopId.of(shopId), CeoId.of(ceoId), content)
        );

        domainEventPublisher.publish(new ReviewOwnerReplyCreatedEvent(
            targetReviewId,
            review.getMemberId(),
            ShopId.of(shopId),
            ReviewOwnerReplyId.of(saved.getId()),
            LocalDateTime.now()
        ));

        return saved.getId();
    }

    /**
     * 리뷰 작성일로부터 {@link ReviewOwnerReply#REPLY_PERIOD_DAYS}일 이내인지 판정한다.
     *
     * <p><b>날짜 경계로 자른다</b> — 초 단위로 비교하면 "30일째 오후에 답변하려니 막힌다"가 되어 원문의
     * "30일 이내"와 어긋난다. 마감일 당일까지는 하루 종일 등록할 수 있다.
     *
     * <p>{@code today}를 파라미터로 받는 이유는 domain-module이 프레임워크-프리라 {@code Clock} 빈을
     * 주입할 수 없고, 여기서 {@code LocalDate.now()}를 직접 호출하면 단위 테스트에서 기한을 고정할 수
     * 없기 때문이다. 해석된 값을 주입받는 순수 계산기 형태를 따른다.
     */
    private void validateWithinReplyPeriod(Review review, LocalDate today) {
        LocalDate deadline = review.getCreatedAt().toLocalDate().plusDays(ReviewOwnerReply.REPLY_PERIOD_DAYS);
        if (today.isAfter(deadline)) {
            throw new BusinessException(ErrorCode.REVIEW_OWNER_REPLY_PERIOD_EXPIRED);
        }
    }

    /**
     * 사장님 답변 내용을 수정한다.
     *
     * <p><b>30일 제한을 적용하지 않는다</b> — 원문이 제한하는 것은 "작성"이므로, 기간이 지난 뒤에도 이미
     * 단 답변의 오타 수정은 허용한다. 알림도 발행하지 않는다(수정마다 알림이 가면 스팸이 된다).
     */
    public void modify(Long shopId, Long reviewId, String content) {
        ReviewId targetReviewId = ReviewId.of(reviewId);
        loadReviewOfShop(targetReviewId, shopId);
        prohibitedWordValidator.validate(content);

        ReviewOwnerReply reply = loadReplyOfReview(targetReviewId);
        reply.updateContent(content);
        reviewOwnerReplyRepository.save(reply);
    }

    /**
     * 사장님 답변을 삭제한다. 삭제 후 같은 리뷰에 다시 답변할 수 있다(단, 재등록은 다시 30일 제한을
     * 받는다 — 제한은 등록 시점에 판정하기 때문이다).
     *
     * <p>수정과 마찬가지로 삭제 자체에는 30일 제한을 걸지 않고, 알림도 발행하지 않는다.
     */
    public void remove(Long shopId, Long reviewId) {
        ReviewId targetReviewId = ReviewId.of(reviewId);
        loadReviewOfShop(targetReviewId, shopId);

        ReviewOwnerReply reply = loadReplyOfReview(targetReviewId);
        reviewOwnerReplyRepository.delete(reply);
    }

    /**
     * 리뷰를 로드하고 그 리뷰가 대상 가게의 것임을 재검증한다.
     *
     * @throws ResourceNotFoundException 리뷰가 없으면 {@code REVIEW_NOT_FOUND}
     * @throws BusinessException 리뷰가 다른 가게 것이면 {@code SHOP_ACCESS_DENIED}
     */
    private Review loadReviewOfShop(ReviewId reviewId, Long shopId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
        if (!review.getShopId().equals(ShopId.of(shopId))) {
            throw new BusinessException(ErrorCode.SHOP_ACCESS_DENIED);
        }
        return review;
    }

    private ReviewOwnerReply loadReplyOfReview(ReviewId reviewId) {
        return reviewOwnerReplyRepository.findByReviewId(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_OWNER_REPLY_NOT_FOUND));
    }
}
