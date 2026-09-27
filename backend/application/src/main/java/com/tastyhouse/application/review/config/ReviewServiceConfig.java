package com.tastyhouse.application.review.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestAttachmentRepository;
import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestRepository;
import com.tastyhouse.application.review.port.out.write.ReviewImageRepository;
import com.tastyhouse.application.review.port.out.write.ReviewLikeRepository;
import com.tastyhouse.application.review.port.out.write.ReviewOwnerReplyRepository;
import com.tastyhouse.application.review.port.out.write.ReviewRepository;
import com.tastyhouse.application.review.port.out.write.ReviewTagRepository;
import com.tastyhouse.application.review.service.ReviewBlindRequestService;
import com.tastyhouse.application.review.service.ReviewLifecycleService;
import com.tastyhouse.application.review.service.ReviewOwnerReplyService;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.application.shop.port.out.write.TagRepository;
import com.tastyhouse.application.shop.service.ProhibitedWordValidator;
import com.tastyhouse.application.shop.service.ShopRequestIndexRecorder;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class ReviewServiceConfig {
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
