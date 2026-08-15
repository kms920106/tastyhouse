package com.tastyhouse.infrastructure.review.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.review.repository.ReviewBlindRequestRepository;
import com.tastyhouse.domain.review.repository.ReviewImageRepository;
import com.tastyhouse.domain.review.repository.ReviewLikeRepository;
import com.tastyhouse.domain.review.repository.ReviewOwnerReplyRepository;
import com.tastyhouse.domain.review.repository.ReviewRepository;
import com.tastyhouse.domain.review.repository.ReviewTagRepository;
import com.tastyhouse.domain.review.service.ReviewBlindRequestService;
import com.tastyhouse.domain.review.service.ReviewLifecycleService;
import com.tastyhouse.domain.review.service.ReviewOwnerReplyService;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;
import com.tastyhouse.domain.shop.repository.TagRepository;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.domain.shop.service.ShopRequestIndexRecorder;

/**
 * review 컨텍스트의 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>도메인 서비스는 {@code @Service} 없는 순수 POJO라 Spring이 스캔할 수 없으므로,
 * 이 컨텍스트에 새 POJO 도메인 서비스를 추가하면 여기에 {@code @Bean}을 추가한다.
 */
@Configuration(proxyBeanMethods = false)
public class ReviewDomainConfig {

    /**
     * 리뷰 생애주기 — 리뷰 본문과 첨부 이미지·태그를 한 트랜잭션에서 함께 저장·정리하고, 좋아요 토글과
     * 통계 갱신 이벤트 발행을 함께 처리하는 오케스트레이션.
     */
    @Bean
    public ReviewLifecycleService reviewLifecycleService(
        ReviewRepository reviewRepository,
        ReviewImageRepository reviewImageRepository,
        ReviewTagRepository reviewTagRepository,
        ReviewLikeRepository reviewLikeRepository,
        TagRepository tagRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        return new ReviewLifecycleService(
            reviewRepository,
            reviewImageRepository,
            reviewTagRepository,
            reviewLikeRepository,
            tagRepository,
            domainEventPublisher
        );
    }

    /**
     * 사장님 답변 규칙 — 대상 리뷰가 그 가게의 것인지 역조회로 재검증하고(IDOR 방어) 금칙어를 검수한 뒤
     * 리뷰당 1건 제약을 지키는 오케스트레이션.
     */
    @Bean
    public ReviewOwnerReplyService reviewOwnerReplyService(
        ReviewOwnerReplyRepository reviewOwnerReplyRepository,
        ReviewRepository reviewRepository,
        ProhibitedWordValidator prohibitedWordValidator
    ) {
        return new ReviewOwnerReplyService(
            reviewOwnerReplyRepository,
            reviewRepository,
            prohibitedWordValidator
        );
    }

    /**
     * 리뷰 게시중단 요청 워크플로 — 승인 시 요청 상태 전이와 리뷰 숨김을 한 트랜잭션에서 함께 반영하고,
     * 모든 상태 전이를 요청처리 현황 인덱스에 동기화하는 오케스트레이션.
     */
    @Bean
    public ReviewBlindRequestService reviewBlindRequestService(
        ReviewBlindRequestRepository reviewBlindRequestRepository,
        ReviewRepository reviewRepository,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        return new ReviewBlindRequestService(
            reviewBlindRequestRepository,
            reviewRepository,
            shopRequestIndexRecorder
        );
    }
}
