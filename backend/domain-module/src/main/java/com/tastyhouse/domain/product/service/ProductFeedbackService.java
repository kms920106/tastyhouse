package com.tastyhouse.domain.product.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductFeedback;
import com.tastyhouse.domain.product.model.ProductFeedbackRead;
import com.tastyhouse.domain.product.model.ProductFeedbackType;
import com.tastyhouse.domain.product.repository.ProductFeedbackReadRepository;
import com.tastyhouse.domain.product.repository.ProductFeedbackRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 메뉴 정보 고객 의견의 불변식 오케스트레이션.
 *
 * <p>{@code Product}와 {@code ProductFeedback} 두 애그리거트를 한 트랜잭션에서 함께 읽고 쓰므로
 * 도메인 서비스에 둔다 — 제보 대상 메뉴의 존재 확인과 가게 비정규화는 모델 하나로 판정할 수 없다.
 *
 * <p><b>중복 제보 방지 기간이 조회 범위(7일)와 같은 값인 것은 우연이 아니다</b> — 점주가 보는 주간
 * 집계 창과 재제보 금지 창이 어긋나면, 창 밖으로 밀려난 제보를 같은 사람이 다시 넣어 집계를 부풀릴 수
 * 있다. 두 값을 같은 상수 하나로 묶어 그 어긋남을 구조적으로 막는다.
 *
 * <p>{@code @Service}·{@code @Transactional} 없는 순수 POJO다. 빈 등록은 infrastructure-module의
 * {@code ProductDomainConfig}가, 트랜잭션 경계는 호출하는 api 모듈의 command 서비스가 소유한다.
 */
public class ProductFeedbackService {

    /**
     * 제보 집계·중복 판정의 공통 창(일). 점주 조회 범위("지난 한 주")와 재제보 금지 기간이 같은 값을
     * 공유해야 창 밖 재제보로 집계가 부풀지 않는다.
     */
    public static final int FEEDBACK_WINDOW_DAYS = 7;

    private final ProductRepository productRepository;
    private final ProductFeedbackRepository productFeedbackRepository;
    private final ProductFeedbackReadRepository productFeedbackReadRepository;

    public ProductFeedbackService(
        ProductRepository productRepository,
        ProductFeedbackRepository productFeedbackRepository,
        ProductFeedbackReadRepository productFeedbackReadRepository
    ) {
        this.productRepository = productRepository;
        this.productFeedbackRepository = productFeedbackRepository;
        this.productFeedbackReadRepository = productFeedbackReadRepository;
    }

    /**
     * 손님의 메뉴 정보 제보를 접수한다.
     *
     * <p>가게는 요청에서 받지 않고 <b>메뉴에서 끌어온다</b> — 클라이언트가 실어 보낸 가게를 그대로 믿으면
     * 남의 가게 목록에 제보를 꽂아 넣을 수 있다.
     *
     * <p>{@code now}를 파라미터로 받아 도메인이 시계를 직접 읽지 않게 한다(테스트 고정 가능).
     */
    public ProductFeedback submit(
        MemberId memberId,
        ProductId productId,
        ProductFeedbackType feedbackType,
        String content,
        LocalDateTime now
    ) {
        Product product = productRepository.findById(productId)
            .filter(found -> !found.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        LocalDateTime windowStart = now.minusDays(FEEDBACK_WINDOW_DAYS);
        if (productFeedbackRepository.existsRecentDuplicate(memberId, productId, feedbackType, windowStart)) {
            throw new BusinessException(ErrorCode.PRODUCT_FEEDBACK_ALREADY_SUBMITTED);
        }

        ProductFeedback feedback = ProductFeedback.of(
            productId, product.getShopId(), memberId, feedbackType, content
        );
        return productFeedbackRepository.save(feedback);
    }

    /**
     * 이 가게에 아직 확인하지 않은 제보가 있는지 — 화면 아이콘의 빨간 점 판정.
     *
     * <p>확인 이력이 없으면 "한 번도 열어보지 않은" 상태이므로, 지난 7일 내 제보가 하나라도 있으면
     * 미확인이다. 이력이 있으면 그 시각 이후 제보만 센다.
     */
    public boolean hasUnread(ShopId shopId, LocalDateTime now) {
        LocalDateTime windowStart = now.minusDays(FEEDBACK_WINDOW_DAYS);
        LocalDateTime since = productFeedbackReadRepository.findByShopId(shopId)
            .map(ProductFeedbackRead::getReadAt)
            // 확인 시각이 조회 창보다 과거면 창 시작을 쓴다 — 창 밖 제보로 빨간 점이 켜지면
            // 목록을 열어도 해당 제보가 보이지 않아 점을 끌 수 없다.
            .filter(readAt -> readAt.isAfter(windowStart))
            .orElse(windowStart);

        return productFeedbackRepository.existsByShopIdAndCreatedAtAfter(shopId, since);
    }

    /**
     * 목록을 연 시점의 확인 처리 — 빨간 점을 끈다. 이력이 없으면 새로 만들고, 있으면 시각을 밀어 올린다.
     */
    public void markRead(ShopId shopId, LocalDateTime now) {
        ProductFeedbackRead feedbackRead = productFeedbackReadRepository.findByShopId(shopId)
            .orElseGet(() -> ProductFeedbackRead.of(shopId, now));
        feedbackRead.markRead(now);
        productFeedbackReadRepository.save(feedbackRead);
    }
}
