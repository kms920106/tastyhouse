package com.tastyhouse.infrastructure.review.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.review.repository.ReviewBlindRequestAttachmentRepository;
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

@Configuration(proxyBeanMethods = false)
public class ReviewDomainConfig {
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

    @Bean
    public ReviewOwnerReplyService reviewOwnerReplyService(
        ReviewOwnerReplyRepository reviewOwnerReplyRepository,
        ReviewRepository reviewRepository,
        ProhibitedWordValidator prohibitedWordValidator,
        DomainEventPublisher domainEventPublisher
    ) {
        return new ReviewOwnerReplyService(
            reviewOwnerReplyRepository,
            reviewRepository,
            prohibitedWordValidator,
            domainEventPublisher
        );
    }

    @Bean
    public ReviewBlindRequestService reviewBlindRequestService(
        ReviewBlindRequestRepository reviewBlindRequestRepository,
        ReviewBlindRequestAttachmentRepository reviewBlindRequestAttachmentRepository,
        ReviewRepository reviewRepository,
        ReviewLifecycleService reviewLifecycleService,
        ShopRequestIndexRecorder shopRequestIndexRecorder,
        DomainEventPublisher domainEventPublisher
    ) {
        return new ReviewBlindRequestService(
            reviewBlindRequestRepository,
            reviewBlindRequestAttachmentRepository,
            reviewRepository,
            reviewLifecycleService,
            shopRequestIndexRecorder,
            domainEventPublisher
        );
    }
}
