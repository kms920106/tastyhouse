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
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스가 선언한다.
 *
 * <p>이벤트 발행은 Spring {@code ApplicationEventPublisher}가 아니라 프레임워크-프리 포트
 * {@link DomainEventPublisher}를 쓴다. 발행된 {@link ReviewCreatedEvent}/{@link ReviewDeletedEvent}는
 * 상품 리뷰 통계를 갱신하는 리스너가 수신한다(리스너 개편은 32-product 소관 — 아래 인계 메모 참고).
 * 좋아요 토글은 이벤트를 발행하지 않는다 — 사유는 {@link #toggleLike} Javadoc 참고.
 *
 * <p>도메인 모델이 순수 POJO라 더티 체킹이 없으므로 변경 후 명시적으로 {@code save}를 호출한다.
 *
 * <p><b>32-product 인계 메모</b> — 이 서비스가 발행하는 이벤트에 반응하는
 * {@code ProductReviewEventListener}는 이번 작업 범위 밖이다. 발행측만 위 포트로 교체했고 리스너는
 * 건드리지 않았다. 다만 리스너가 쓰던 리뷰 통계 조회는 write 포트 순수화로 {@code ReviewRepository}에서
 * 사라졌으므로, 그 자리를 도메인 포트
 * {@code com.tastyhouse.domain.product.port.ProductReviewStatisticsPort}(infra 어댑터가
 * {@code ReviewQueryDao}에 위임)로 대체해 두었다. 리스너를
 * {@code infrastructure/product/listener/}로 옮길 때 이 포트를 그대로 쓰거나, infra 안에서는 DAO를
 * 직접 주입하도록 정리하면 된다.
 *
 * <p>공개 시그니처(32-product 참조용):
 * <pre>
 * ReviewRegistration register(Long shopId, Long productId, MemberId memberId, Long orderId,
 *                            Integer tasteRating, Integer amountRating, Integer priceRating,
 *                            String content, List&lt;Long&gt; uploadedFileIds, List&lt;String&gt; tags)
 * ReviewRegistration modify(ReviewId reviewId, MemberId memberId,
 *                           Integer tasteRating, Integer amountRating, Integer priceRating,
 *                           String content, List&lt;Long&gt; uploadedFileIds, List&lt;String&gt; tags)
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
        List<String> tags
    ) {
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
            orderId
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
        List<String> tags
    ) {
        Review review = reviewRepository.findByIdAndMemberId(reviewId, memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_ACCESS_DENIED));

        review.updateContent(
            content,
            averageRating(tasteRating, amountRating, priceRating),
            tasteRating.doubleValue(),
            amountRating.doubleValue(),
            priceRating.doubleValue(),
            null, null, null, false
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
     * no-op이어서 P9(도메인 이벤트 정비)에서 제거했다. 형제 이벤트인 {@link ReviewCreatedEvent}/
     * {@link ReviewDeletedEvent}가 소비되는 이유는 상품 리뷰 통계(평점·리뷰 수) 갱신인데, 좋아요는 그
     * 통계에 포함되지 않아 같은 계열이어도 소비 수요가 없다.
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
