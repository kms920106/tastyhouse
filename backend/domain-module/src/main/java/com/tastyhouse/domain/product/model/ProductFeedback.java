package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 메뉴 정보에 대한 고객 의견 순수 도메인 모델.
 *
 * <p><b>리뷰({@code MENU_REVIEW})와 다르다</b> — 리뷰는 "음식이 어땠는지"이고 이것은 "등록된 정보가
 * 틀렸다"는 제보다. 별점을 남기지 않고 점수에도 반영되지 않으며, 점주가 고쳐야 할 대상은 음식이 아니라
 * 메뉴 등록 정보다. 인접 선례는 {@code BugReport}(서비스 오류 제보)의 "제보 + 상태" 구조다.
 *
 * <p><b>{@code shopId}를 비정규화해 함께 들고 있는 이유</b>: 점주 목록 조회가 "이 가게의 지난 7일 제보"를
 * 가게 단위로 읽는데, 이 컬럼이 없으면 매 조회가 {@code PRODUCT}와 조인해야 한다. 제보 시점의 가게를
 * 고정해 두는 편이 조회도 빠르고, 메뉴가 다른 가게로 연결되더라도 제보가 접수된 가게가 흔들리지 않는다.
 *
 * <p><b>{@code memberId}는 저장하되 점주에게 내려보내지 않는다</b>. 저장은 중복 제보 방지(같은 회원·같은
 * 메뉴·같은 유형 7일 내 1회)에 필요하지만, 점주가 제보자를 식별하면 보복 우려가 있고 제보의 목적은
 * 정보 수정이지 손님 응대가 아니다. 조회 응답에서의 제외는 query 계층이 책임진다.
 *
 * <p>모든 필드가 {@code final}이다 — 제보는 <b>수정되지 않는 사실 기록</b>이라 전이 메서드를 두지 않는
 * 것이 "변경 경로가 없다"의 구조적 표현이다.
 *
 * <p>영속화는 infrastructure-module의 {@code ProductFeedbackJpaEntity} +
 * {@code ProductFeedbackMapper}가 담당한다.
 */
public class ProductFeedback {

    private static final int CONTENT_MAX_LENGTH = 500;

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ProductId productId;
    private final ShopId shopId; // 제보 시점의 가게 (목록 조회용 비정규화)
    private final MemberId memberId; // 중복 제보 판정용 — 점주에게 노출하지 않는다
    private final ProductFeedbackType feedbackType;
    private final String content; // ETC 유형일 때 필수, 그 외에는 null 허용
    private final LocalDateTime createdAt; // 주간 집계의 기준 시각
    private final LocalDateTime updatedAt;

    private ProductFeedback(
        Long id,
        ProductId productId,
        ShopId shopId,
        MemberId memberId,
        ProductFeedbackType feedbackType,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.productId = productId;
        this.shopId = shopId;
        this.memberId = memberId;
        this.feedbackType = feedbackType;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 제보를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     *
     * <p>내용 불변식을 강제한다 — {@code ETC}면 내용 필수, 그 외 유형에서도 내용을 함께 보낼 수 있으나
     * 500자를 넘길 수 없다. 공백만 담긴 내용은 없는 것으로 본다(빈 문자열로 필수 검증을 우회하는 것을 막는다).
     */
    public static ProductFeedback of(
        ProductId productId,
        ShopId shopId,
        MemberId memberId,
        ProductFeedbackType feedbackType,
        String content
    ) {
        String normalizedContent = normalizeContent(content);
        validateContent(feedbackType, normalizedContent);

        return new ProductFeedback(null, productId, shopId, memberId, feedbackType, normalizedContent, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층 전용이며,
     * {@link #of}와 달리 내용 불변식을 검증하지 않는다 — 불변식 도입 이전 데이터도 로드는 가능해야 한다.
     */
    public static ProductFeedback reconstitute(
        Long id,
        ProductId productId,
        ShopId shopId,
        MemberId memberId,
        ProductFeedbackType feedbackType,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ProductFeedback(id, productId, shopId, memberId, feedbackType, content, createdAt, updatedAt);
    }

    /** 공백만 담긴 내용은 없는 것으로 본다 — 빈 문자열로 {@code ETC} 필수 검증을 우회하는 것을 막는다. */
    private static String normalizeContent(String content) {
        if (content == null) {
            return null;
        }
        String trimmed = content.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static void validateContent(ProductFeedbackType feedbackType, String content) {
        if (feedbackType.requiresContent() && content == null) {
            throw new BusinessException(ErrorCode.PRODUCT_FEEDBACK_CONTENT_REQUIRED);
        }
        if (content != null && content.length() > CONTENT_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.PRODUCT_FEEDBACK_CONTENT_TOO_LONG);
        }
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public ProductFeedbackType getFeedbackType() {
        return this.feedbackType;
    }

    public String getContent() {
        return this.content;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
