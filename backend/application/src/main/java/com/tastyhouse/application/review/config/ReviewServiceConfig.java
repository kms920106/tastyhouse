package com.tastyhouse.application.review.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestAttachmentStatePort;
import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestStatePort;
import com.tastyhouse.application.review.port.out.write.ReviewCommentStatePort;
import com.tastyhouse.application.review.port.out.write.ReviewImageStatePort;
import com.tastyhouse.application.review.port.out.write.ReviewLikeStatePort;
import com.tastyhouse.application.review.port.out.write.ReviewOwnerReplyStatePort;
import com.tastyhouse.application.review.port.out.write.ReviewReplyStatePort;
import com.tastyhouse.application.review.port.out.write.ReviewStatePort;
import com.tastyhouse.application.review.port.out.write.ReviewTagStatePort;
import com.tastyhouse.application.review.port.out.write.ShopReviewDisplaySettingStatePort;
import com.tastyhouse.application.review.service.ReviewBlindRequestService;
import com.tastyhouse.application.review.service.ReviewLifecycleService;
import com.tastyhouse.application.review.service.ReviewOwnerReplyService;
import com.tastyhouse.application.review.store.ReviewBlindRequestAttachmentRepository;
import com.tastyhouse.application.review.store.ReviewBlindRequestAttachmentStore;
import com.tastyhouse.application.review.store.ReviewBlindRequestRepository;
import com.tastyhouse.application.review.store.ReviewBlindRequestStore;
import com.tastyhouse.application.review.store.ReviewCommentRepository;
import com.tastyhouse.application.review.store.ReviewCommentStore;
import com.tastyhouse.application.review.store.ReviewImageRepository;
import com.tastyhouse.application.review.store.ReviewImageStore;
import com.tastyhouse.application.review.store.ReviewLikeRepository;
import com.tastyhouse.application.review.store.ReviewLikeStore;
import com.tastyhouse.application.review.store.ReviewOwnerReplyRepository;
import com.tastyhouse.application.review.store.ReviewOwnerReplyStore;
import com.tastyhouse.application.review.store.ReviewReplyRepository;
import com.tastyhouse.application.review.store.ReviewReplyStore;
import com.tastyhouse.application.review.store.ReviewRepository;
import com.tastyhouse.application.review.store.ReviewStore;
import com.tastyhouse.application.review.store.ReviewTagRepository;
import com.tastyhouse.application.review.store.ReviewTagStore;
import com.tastyhouse.application.review.store.ShopReviewDisplaySettingRepository;
import com.tastyhouse.application.review.store.ShopReviewDisplaySettingStore;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.application.shop.store.TagRepository;
import com.tastyhouse.application.shop.service.ProhibitedWordValidator;
import com.tastyhouse.application.shop.service.ShopRequestIndexRecorder;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class ReviewServiceConfig {
    @Bean
    public ReviewRepository reviewRepository(ReviewStatePort reviewStatePort) {
        return new ReviewStore(reviewStatePort);
    }

    @Bean
    public ReviewImageRepository reviewImageRepository(ReviewImageStatePort reviewImageStatePort) {
        return new ReviewImageStore(reviewImageStatePort);
    }

    @Bean
    public ReviewLikeRepository reviewLikeRepository(ReviewLikeStatePort reviewLikeStatePort) {
        return new ReviewLikeStore(reviewLikeStatePort);
    }

    @Bean
    public ReviewCommentRepository reviewCommentRepository(ReviewCommentStatePort reviewCommentStatePort) {
        return new ReviewCommentStore(reviewCommentStatePort);
    }

    @Bean
    public ReviewReplyRepository reviewReplyRepository(ReviewReplyStatePort reviewReplyStatePort) {
        return new ReviewReplyStore(reviewReplyStatePort);
    }

    @Bean
    public ReviewOwnerReplyRepository reviewOwnerReplyRepository(ReviewOwnerReplyStatePort reviewOwnerReplyStatePort) {
        return new ReviewOwnerReplyStore(reviewOwnerReplyStatePort);
    }

    @Bean
    public ReviewTagRepository reviewTagRepository(ReviewTagStatePort reviewTagStatePort) {
        return new ReviewTagStore(reviewTagStatePort);
    }

    @Bean
    public ReviewBlindRequestRepository reviewBlindRequestRepository(ReviewBlindRequestStatePort reviewBlindRequestStatePort) {
        return new ReviewBlindRequestStore(reviewBlindRequestStatePort);
    }

    @Bean
    public ReviewBlindRequestAttachmentRepository reviewBlindRequestAttachmentRepository(ReviewBlindRequestAttachmentStatePort reviewBlindRequestAttachmentStatePort) {
        return new ReviewBlindRequestAttachmentStore(reviewBlindRequestAttachmentStatePort);
    }

    @Bean
    public ShopReviewDisplaySettingRepository shopReviewDisplaySettingRepository(ShopReviewDisplaySettingStatePort shopReviewDisplaySettingStatePort) {
        return new ShopReviewDisplaySettingStore(shopReviewDisplaySettingStatePort);
    }

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
