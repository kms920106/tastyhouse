package com.tastyhouse.domain.menureview.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.event.MenuReviewCreatedEvent;
import com.tastyhouse.domain.menureview.event.MenuReviewDeletedEvent;
import com.tastyhouse.domain.menureview.event.MenuReviewRatingChangedEvent;
import com.tastyhouse.domain.menureview.model.MenuReview;
import com.tastyhouse.domain.menureview.repository.MenuReviewRepository;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * 메뉴 평가 생애주기 불변식(도메인 서비스).
 *
 * <p><b>매장 리뷰({@code Review})의 존재 여부를 확인하지 않는다.</b> 매장 리뷰가 없어도 메뉴 평가는
 * 등록된다 — "어느 쪽을 먼저 하든, 하나만 하든 성립한다"는 이 설계의 최상위 원칙이 여기서 실제로
 * 구현된다. review 컨텍스트를 참조하지 않으므로 그 원칙이 코드 구조로도 드러난다.
 *
 * <p><b>이 서비스가 검증하지 <em>않는</em> 것</b> — 주문 항목의 존재, 그 주문의 소유자, 대상 상품의
 * 평가 제외 여부는 전부 order·product 컨텍스트의 데이터라 여기서 판정할 수 없다(컨텍스트 경계 규칙).
 * 호출부(web-api {@code MenuReviewCommandService})가 그 세 가지를 먼저 판정해 이미 확정된 값
 * ({@code shopId}·{@code productId}·{@code orderId})만 넘긴다. 그 값을 클라이언트가 보낸 것으로
 * 채우면 다른 상품에 평점을 붙일 수 있으므로, 호출부는 반드시 {@code ORDER_PRODUCT}에서 읽어 넘긴다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code MenuReviewDomainConfig}가 담당한다. 트랜잭션 경계는 호출하는 api 모듈 CommandService가 연다.
 *
 * <p>{@link DomainEventPublisher}는 생성자 <b>필수</b> 의존이다 — 새 전이를 추가할 때 재집계 이벤트
 * 배선이 필요하다는 사실이 컴파일 시점에 드러나게 하기 위함이다.
 */
public class MenuReviewLifecycleService {

    private final MenuReviewRepository menuReviewRepository;
    private final DomainEventPublisher domainEventPublisher;

    public MenuReviewLifecycleService(
        MenuReviewRepository menuReviewRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        this.menuReviewRepository = menuReviewRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * 메뉴 평가를 등록한다.
     *
     * <p>{@code existsByOrderProductId} 검사로 409를 돌려주지만, 동시 요청의 최종 방어선은
     * {@code UNIQUE(order_product_id)}다 — 검사와 삽입 사이의 경합은 애플리케이션 코드로 막을 수 없다.
     *
     * @return 생성된 메뉴 평가 식별자
     */
    public Long register(
        MemberId memberId,
        ShopId shopId,
        ProductId productId,
        OrderId orderId,
        OrderProductId orderProductId,
        Integer rating,
        String comment
    ) {
        if (menuReviewRepository.existsByOrderProductId(orderProductId)) {
            throw new BusinessException(ErrorCode.MENU_REVIEW_ALREADY_EXISTS);
        }

        MenuReview saved = menuReviewRepository.save(
            MenuReview.of(memberId, shopId, productId, orderId, orderProductId, rating, comment)
        );

        domainEventPublisher.publish(new MenuReviewCreatedEvent(
            saved.getMenuReviewId(),
            memberId,
            shopId,
            productId,
            LocalDateTime.now()
        ));

        return saved.getId();
    }

    /**
     * 메뉴 평가를 수정한다(본인 평가만). 작성 근거인 {@code orderProductId}는 바꿀 수 없다.
     *
     * <p>평점이 바뀌면 상품 평균이 달라지므로 {@link MenuReviewRatingChangedEvent}를 발행한다 — 코멘트만
     * 바뀐 경우도 함께 발행한다(수신 측이 어차피 전체 재집계라 분기의 실익이 없고, 분기를 두면 "평점이
     * 안 바뀌었는지" 판단이 두 곳으로 갈린다).
     */
    public void modify(MenuReviewId menuReviewId, MemberId memberId, Integer rating, String comment) {
        MenuReview menuReview = loadOwnedBy(menuReviewId, memberId);

        menuReview.updateRating(rating, comment);
        menuReviewRepository.save(menuReview);

        domainEventPublisher.publish(new MenuReviewRatingChangedEvent(
            menuReviewId,
            memberId,
            menuReview.getShopId(),
            menuReview.getProductId(),
            LocalDateTime.now()
        ));
    }

    /**
     * 메뉴 평가를 삭제한다(본인 평가만). 삭제되면 그 주문 항목에 다시 평가를 남길 수 있다.
     */
    public void remove(MenuReviewId menuReviewId, MemberId memberId) {
        MenuReview menuReview = loadOwnedBy(menuReviewId, memberId);

        menuReviewRepository.deleteById(menuReviewId);

        domainEventPublisher.publish(new MenuReviewDeletedEvent(
            menuReviewId,
            memberId,
            menuReview.getShopId(),
            menuReview.getProductId(),
            LocalDateTime.now()
        ));
    }

    /**
     * 본인 평가를 로드한다. 남의 평가는 존재 여부와 무관하게 403으로 응답한다 — 404로 갈리면 "그 id의
     * 평가가 존재하는가"가 응답 코드로 새어나간다.
     *
     * @throws BusinessException 본인 평가가 아니거나 존재하지 않으면 {@code MENU_REVIEW_ACCESS_DENIED}
     */
    private MenuReview loadOwnedBy(MenuReviewId menuReviewId, MemberId memberId) {
        return menuReviewRepository.findByIdAndMemberId(menuReviewId, memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_REVIEW_ACCESS_DENIED));
    }
}
