package com.tastyhouse.domain.review.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.event.ReviewCreatedEvent;
import com.tastyhouse.domain.review.event.ReviewDeletedEvent;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.model.ReviewImage;
import com.tastyhouse.domain.review.model.ReviewLike;
import com.tastyhouse.domain.review.model.ReviewTag;
import com.tastyhouse.domain.review.repository.ReviewImageRepository;
import com.tastyhouse.domain.review.repository.ReviewLikeRepository;
import com.tastyhouse.domain.review.repository.ReviewRepository;
import com.tastyhouse.domain.review.repository.ReviewTagRepository;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.model.Tag;
import com.tastyhouse.domain.shop.repository.TagRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.TagId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * 리뷰 생애주기 불변식(도메인 서비스).
 *
 * <p>리뷰 등록·수정·삭제는 {@code Review} 본문과 첨부 이미지({@code ReviewImage})·태그
 * ({@code ReviewTag}/{@code Tag}) 여러 애그리거트를 한 트랜잭션에서 함께 저장·정리해야 하는 원자
 * 연산이다. 등록 시 이미지·태그가 함께 남지 않으면 리뷰가 반쪽으로 저장되고, 삭제 시 이미지·태그를
 * 함께 지우지 않으면 고아 행이 남는다. 이런 크로스 애그리거트 불변식 오케스트레이션(분류 C)이므로
 * 도메인 계층에 두어, 트리거 액터(회원 본인 · 관리자)가 달라도 규칙이 갈리지 않게 한다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code ReviewDomainConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스가 선언한다.
 *
 * <p>이벤트 발행은 Spring {@code ApplicationEventPublisher}가 아니라 프레임워크-프리 포트
 * {@link DomainEventPublisher}를 쓴다. <b>{@link ReviewCreatedEvent}/{@link ReviewDeletedEvent}는 현재
 * 소비자가 0이다</b> — 상품 평점 재집계의 근거가 MENU_REVIEW로 이관되면서 구독이
 * {@code ProductMenuReviewEventListener}로 옮겨갔기 때문이다. 발행측을 그대로 두는 이유는 각 이벤트
 * Javadoc 참고.
 * 좋아요 토글은 이벤트를 발행하지 않는다 — 사유는 {@link #toggleLike} Javadoc 참고.
 *
 * <p>도메인 모델이 순수 POJO라 더티 체킹이 없으므로 변경 후 명시적으로 {@code save}를 호출한다.
 *
 * <p><b>상품 평점과의 관계</b> — 과거에는 이 서비스의 이벤트가 {@code PRODUCT.rating} 재집계를
 * 트리거했으나, 그 근거가 MENU_REVIEW로 완전히 이관되어 지금은 {@code ProductMenuReviewEventListener}가
 * {@code MenuReview*} 이벤트만 구독한다. 따라서 <b>리뷰 등록·수정·삭제는 상품 평점을 바꾸지 않는다</b>.
 * 재집계 구독을 여기로 되돌리면 근거가 둘로 갈려 같은 갱신이 두 번 돈다.
 *
 * <p>공개 시그니처:
 * <pre>
 * ReviewRegistration register(ShopId shopId, ProductId productId, MemberId memberId, OrderId orderId,
 *                            Integer tasteRating, Integer amountRating, Integer priceRating,
 *                            String content, List&lt;Long&gt; uploadedFileIds, List&lt;String&gt; tags,
 *                            boolean ownerOnly, Integer deliveryRating, String deliveryComment)
 * ReviewRegistration modify(ReviewId reviewId, MemberId memberId,
 *                           Integer tasteRating, Integer amountRating, Integer priceRating,
 *                           String content, List&lt;Long&gt; uploadedFileIds, List&lt;String&gt; tags,
 *                           Integer deliveryRating, String deliveryComment)
 * void removeOwnedBy(ReviewId reviewId, MemberId memberId, Long productId)
 * void remove(ReviewId reviewId)
 * boolean toggleLike(ReviewId reviewId, MemberId memberId)
 * </pre>
 */
public class ReviewLifecycleService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewTagRepository reviewTagRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final TagRepository tagRepository;
    private final DomainEventPublisher domainEventPublisher;

    public ReviewLifecycleService(
        ReviewRepository reviewRepository,
        ReviewImageRepository reviewImageRepository,
        ReviewTagRepository reviewTagRepository,
        ReviewLikeRepository reviewLikeRepository,
        TagRepository tagRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        this.reviewRepository = reviewRepository;
        this.reviewImageRepository = reviewImageRepository;
        this.reviewTagRepository = reviewTagRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.tagRepository = tagRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * 리뷰 등록 — 본문 저장과 이미지·태그 적재, 통계 갱신 이벤트 발행이 함께 일어난다.
     *
     * <p>{@code ownerOnly}(사장님만보기)는 등록 시점에만 정해진다. 전환은 불허이므로
     * {@link #modify}에는 대응 파라미터가 없다.
     *
     * <p>{@code deliveryRating}/{@code deliveryComment}(배달 평가)는 배달 주문에만 남길 수 있다. 주문유형
     * 판정은 order 컨텍스트를 알아야 하므로 이 서비스가 아니라 호출부(web-api {@code ReviewCommandService})가
     * 수행한다 — 여기서 하면 review 컨텍스트가 order 모델을 직접 참조하게 된다. 총점
     * ({@link #averageRating})에는 포함하지 않는다.
     *
     * <p>{@code orderId}가 있으면 같은 주문·같은 상품에 이미 리뷰가 있는지 검사해 중복 등록을 막는다
     * ({@link ErrorCode#REVIEW_ALREADY_EXISTS}). REVIEW 테이블에 주문상품 단위 식별자가 없어
     * {@code order_id}+{@code product_id} 조합으로 판정하므로, 한 주문에 동일 상품을 2개 이상 담은
     * 경우 정당한 추가 리뷰까지 막힐 수 있다(알려진 한계로 승인됨). {@code orderId}가 없으면(주문 인증
     * 없이 등록) 이 조합을 판정할 근거가 없으므로 검사를 생략한다.
     */
    public ReviewRegistration register(
        ShopId shopId,
        ProductId productId,
        MemberId memberId,
        OrderId orderId,
        Integer tasteRating,
        Integer amountRating,
        Integer priceRating,
        String content,
        List<Long> uploadedFileIds,
        List<String> tags,
        boolean ownerOnly,
        Integer deliveryRating,
        String deliveryComment
    ) {
        if (orderId != null && reviewRepository.existsByOrderIdAndProductId(orderId, productId)) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.of(
            shopId,
            productId,
            memberId,
            content,
            averageRating(tasteRating, amountRating, priceRating),
            tasteRating.doubleValue(),
            amountRating.doubleValue(),
            priceRating.doubleValue(),
            null, null, null, false,
            orderId,
            ownerOnly,
            deliveryRating,
            deliveryComment
        );

        Review saved = reviewRepository.save(review);

        List<Long> savedFileIds = saveImages(saved.getReviewId(), uploadedFileIds);
        List<String> savedTags = saveTags(saved.getReviewId(), tags);

        domainEventPublisher.publish(new ReviewCreatedEvent(
            saved.getReviewId(),
            memberId,
            shopId,
            productId,
            LocalDateTime.now()
        ));

        return new ReviewRegistration(saved, savedFileIds, savedTags);
    }

    /**
     * 리뷰 수정 — 본인 리뷰만 수정할 수 있다. 이미지·태그는 전량 교체한다(기존 삭제 후 재적재).
     */
    public ReviewRegistration modify(
        ReviewId reviewId,
        MemberId memberId,
        Integer tasteRating,
        Integer amountRating,
        Integer priceRating,
        String content,
        List<Long> uploadedFileIds,
        List<String> tags,
        Integer deliveryRating,
        String deliveryComment
    ) {
        Review review = reviewRepository.findByIdAndMemberId(reviewId, memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_ACCESS_DENIED));

        review.updateContent(
            content,
            averageRating(tasteRating, amountRating, priceRating),
            tasteRating.doubleValue(),
            amountRating.doubleValue(),
            priceRating.doubleValue(),
            null, null, null, false,
            deliveryRating,
            deliveryComment
        );

        Review saved = reviewRepository.save(review);

        reviewImageRepository.deleteByReviewId(reviewId);
        reviewTagRepository.deleteByReviewId(reviewId);

        List<Long> savedFileIds = saveImages(reviewId, uploadedFileIds);
        List<String> savedTags = saveTags(reviewId, tags);

        return new ReviewRegistration(saved, savedFileIds, savedTags);
    }

    /**
     * 리뷰 삭제(본인) — 소유권을 확인한 뒤 이미지·태그를 함께 정리한다.
     *
     * <p>{@code productId}는 호출부가 이미 알고 있는 값을 그대로 이벤트에 싣는다(삭제 전 재조회 불필요).
     */
    public void removeOwnedBy(ReviewId reviewId, MemberId memberId, ProductId productId) {
        reviewRepository.findByIdAndMemberId(reviewId, memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_ACCESS_DENIED));

        deleteWithChildren(reviewId);

        domainEventPublisher.publish(new ReviewDeletedEvent(
            reviewId,
            memberId,
            productId,
            LocalDateTime.now()
        ));
    }

    /**
     * 리뷰 삭제(관리자) — 소유권 검증 없이 삭제한다. 이벤트에 실을 작성자·상품은 삭제 전 리뷰에서 읽는다.
     */
    public void remove(ReviewId reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        deleteWithChildren(reviewId);

        domainEventPublisher.publish(new ReviewDeletedEvent(
            reviewId,
            review.getMemberId(),
            review.getProductId(),
            LocalDateTime.now()
        ));
    }

    /**
     * 좋아요 토글 — 이미 눌렀으면 취소, 아니면 등록한다. 반환값은 토글 <b>후</b> 좋아요 상태다.
     *
     * <p>이벤트를 발행하지 않는다 — 과거 {@code ReviewLikedEvent}를 발행했으나 수신 리스너가 없는
     * no-op이어서 P9(도메인 이벤트 정비)에서 제거했다.
     *
     * <p>형제 이벤트인 {@link ReviewCreatedEvent}/{@link ReviewDeletedEvent}도 <b>현재 소비자가 0이다</b> —
     * 과거에는 상품 리뷰 통계(평점·리뷰 수) 갱신에 소비됐으나, 상품 별점의 근거가 {@code MENU_REVIEW}로
     * 이관되면서 구독이 그쪽으로 옮겨갔다. 두 이벤트를 미소비라는 이유로 제거하지 않는 근거는 각 이벤트
     * record의 Javadoc에 있다. 좋아요는 애초에 상품 통계와 무관해 이벤트 자체를 두지 않는다.
     */
    public boolean toggleLike(ReviewId reviewId, MemberId memberId) {
        boolean liked = !reviewLikeRepository.existsByReviewIdAndMemberId(reviewId, memberId);

        if (liked) {
            reviewLikeRepository.save(ReviewLike.of(reviewId, memberId));
        } else {
            reviewLikeRepository.deleteByReviewIdAndMemberId(reviewId, memberId);
        }

        return liked;
    }

    /**
     * 리뷰 본문과 그에 딸린 이미지·태그를 함께 삭제한다(고아 행 방지).
     */
    private void deleteWithChildren(ReviewId reviewId) {
        reviewImageRepository.deleteByReviewId(reviewId);
        reviewTagRepository.deleteByReviewId(reviewId);
        reviewRepository.deleteById(reviewId);
    }

    /**
     * 총점 — 맛·양·가격 세 항목 평균을 소수 첫째 자리로 반올림한다.
     */
    private double averageRating(Integer tasteRating, Integer amountRating, Integer priceRating) {
        return Math.round((tasteRating + amountRating + priceRating) / 3.0 * 10.0) / 10.0;
    }

    private List<Long> saveImages(ReviewId reviewId, List<Long> uploadedFileIds) {
        if (uploadedFileIds == null || uploadedFileIds.isEmpty()) {
            return List.of();
        }

        List<ReviewImage> images = new ArrayList<>();
        for (int i = 0; i < uploadedFileIds.size(); i++) {
            images.add(ReviewImage.of(reviewId, UploadedFileId.of(uploadedFileIds.get(i)), i + 1));
        }
        reviewImageRepository.saveAll(images);

        return uploadedFileIds;
    }

    /**
     * 태그 적재 — 태그명은 전역 {@code Tag} 사전에 없으면 새로 만들고, 리뷰-태그 연결만 리뷰별로 쌓는다.
     */
    private List<String> saveTags(ReviewId reviewId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }

        List<ReviewTag> reviewTags = tagNames.stream()
            .map(tagName -> {
                Tag tag = tagRepository.findByTagName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.of(tagName)));
                return ReviewTag.of(reviewId, TagId.of(tag.getId()));
            })
            .toList();
        reviewTagRepository.saveAll(reviewTags);

        return tagNames;
    }
}
